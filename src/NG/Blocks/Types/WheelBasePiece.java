package NG.Blocks.Types;

import NG.DataStructures.Generic.Color4f;
import NG.Entities.Entity;
import NG.Rendering.MatrixStack.SGL;
import NG.Storable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 26-8-2019.
 */
public class WheelBasePiece extends AbstractPiece {
    private final List<Hinge> hinges;
    private PieceTypeHinge type;

    public WheelBasePiece(PieceTypeHinge type, Vector3ic position, int rotation, Color4f color) {
        super(position, rotation, color);
        this.type = type;

        int nrOfHinges = type.hingeOffsets.size();
        this.hinges = new ArrayList<>(nrOfHinges);
        for (int i = 0; i < nrOfHinges; i++) {
            hinges.add(new Hinge(type.hingeOffsets.get(i)));
        }
    }

    @Override
    protected void drawPiece(SGL gl, Entity entity, float renderTime) {
        type.draw(gl, entity);

        for (Hinge hinge : hinges) {
            hinge.draw(gl, entity, renderTime);
        }
    }

    @Override
    public PieceType getType() {
        return type;
    }

    @Override
    public AbstractPiece copy() {
        return new WheelBasePiece(type, position, rotation, color);
    }

    @Override
    public void write(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException {
        out.writeInt(typeMap.get(type));

        out.writeInt(hinges.size());
        for (Hinge h : hinges) {
            Storable.writeVector3f(out, h.offset);
            h.wheel.writeToDataStream(out, typeMap);
        }
    }

    public WheelBasePiece(DataInputStream in, PieceType[] typeMap) throws IOException {
        super(in);
        type = (PieceTypeHinge) typeMap[in.readInt()];

        int nrOfHinges = in.readInt();
        hinges = new ArrayList<>(nrOfHinges);
        for (int i = 0; i < nrOfHinges; i++) {
            hinges.add(new Hinge(
                    Storable.readVector3f(in),
                    new WheelPiece(in, typeMap)
            ));
        }
    }

    /**
     * @return a list that allows setting and getting the wheels of this wheel base.
     * The list is modifiable and fixed-sized
     */
    public List<WheelPiece> getWheels(){
        return new WheelModificationList(hinges);
    }

    private static class WheelModificationList extends AbstractList<WheelPiece> {
        private final List<Hinge> hinges;

        public WheelModificationList(List<Hinge> hinges) {
            this.hinges = hinges;
        }

        @Override
        public WheelPiece get(int index) {
            return hinges.get(index).wheel;
        }

        @Override
        public WheelPiece set(int index, WheelPiece newElt) {
            Hinge hinge = hinges.get(index);
            WheelPiece prev = hinge.wheel;
            hinge.wheel = newElt;
            return prev;
        }

        @Override
        public void add(int index, WheelPiece element) {
            set(index, element);
        }

        @Override
        public boolean add(WheelPiece wheelPiece) {
            for (int i = 0; i < size(); i++) {
                if (get(i) != null){
                    set(i, wheelPiece);
                    return true;
                }
            }
            throw new IllegalStateException("No empty hinges found");
        }

        @Override
        public int size() {
            return hinges.size();
        }
    };

    private static class Hinge {
        private final Vector3fc offset;
        private WheelPiece wheel;

        /**
         * @param offset the offset of the wheel, relative to the parent block
         */
        public Hinge(Vector3fc offset) {
            this.offset = new Vector3f(offset).sub(BLOCK_BASE/2, BLOCK_BASE/2, 0); // negate translation to stud;
        }

        /**
         * for creation by input stream
         */
        private Hinge(Vector3f offset, WheelPiece wheel) {
            this.offset = offset;
            this.wheel = wheel;
        }

        public void draw(SGL gl, Entity entity, float renderTime) {
            if (wheel == null) return;
            gl.pushMatrix();
            {
                gl.translate(offset);
                gl.rotateQuarter(0, 1, 0);

                wheel.draw(gl, entity, renderTime);
            }
            gl.popMatrix();
        }
    }

    @Override
    public String toString() {
        return "WheelBase " + getHitBox();
    }
}
