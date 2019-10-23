package NG.Blocks.Types;

import NG.Blocks.BlockSubGrid;
import NG.DataStructures.Generic.Color4f;
import NG.DataStructures.Generic.Pair;
import NG.DataStructures.Vector3fx;
import NG.Entities.Entity;
import NG.Entities.MutableState;
import NG.Entities.Projectiles.DeathIcosahedron;
import NG.Entities.Projectiles.Projectile;
import NG.Entities.State;
import NG.Rendering.MatrixStack.SGL;
import NG.Shapes.Shape;
import NG.Tools.Toolbox;
import org.joml.Vector3f;
import org.joml.Vector3ic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A gun block, which is stateful and can produce projectile objects.
 */
public class GunPiece extends AbstractPiece {
    protected final PieceTypeGun type;
    private float timeNextFire;
    private boolean isFiring = false;
    private List<Pair<Float, Vector3f>> storedBullets = new ArrayList<>();

    private float phi; // yaw
    private float theta; // pitch

    protected GunPiece(Vector3ic position, int zRotation, Color4f color, PieceTypeGun type) {
        super(position, zRotation, color);
        this.type = type;
        timeNextFire = type.reloadTime;
    }

    /**
     * updates the aiming and firing events of this block
     * @param gameTime the current time
     * @param deltaTime the time since last update
     * @param doFire if true, this gun fires
     * @param rotation the target rotation of this gun in local space
     * @param elevation the target elevation of this gun in local space
     */
    public void update(float gameTime, float deltaTime, boolean doFire, float rotation, float elevation){
        Vector3f prevGunDirection = getAimDirection();

        float maxMove = type.rotationSpeed * deltaTime;
        phi = limit(phi, rotation, maxMove);
        theta = limit(theta, elevation, maxMove);

        Vector3f newGunDirection = getAimDirection();

        if (doFire) {
            if (!isFiring && timeNextFire < gameTime){
                timeNextFire = gameTime;
            }

            while (timeNextFire <= gameTime) {
                // fire at timeNextFired
                float fraction = (gameTime - timeNextFire) / (deltaTime);
                Vector3f projectileDirection = prevGunDirection.lerp(newGunDirection, fraction);
                storedBullets.add(new Pair<>(timeNextFire, projectileDirection));

                timeNextFire += type.reloadTime;
            }
            isFiring = true;

        } else {
            isFiring = false;
        }
    }

    @Override
    protected void drawPiece(SGL gl, Entity entity, float renderTime) {
        gl.pushMatrix();
        {
            type.bottomPiece.draw(gl, entity);

            gl.translate(type.jointOffset);
            gl.rotate(phi, 0, 0, 1);
            gl.rotate(theta, 0, 1, 0);

            type.topPiece.draw(gl, entity);
        }
        gl.popMatrix();
    }

    /**
     * @return the normalized aim direction at the time of the last call to {@link #update(float, float, boolean, float, float)}
     */
    public Vector3f getAimDirection() {
        float eyeX = (float) (Math.sin(theta) * Math.cos(phi));
        float eyeY = (float) (Math.sin(theta) * Math.sin(phi));
        float eyeZ = (float) Math.cos(theta);

        return new Vector3f(eyeX, eyeY, eyeZ).normalize();
    }

    /**
     * @param current the current value of some element
     * @param target the target new value of some element
     * @param maxDiff the maximum change of the element
     * @return the value closest to target such that it is within maxDiff from current.
     */
    private float limit(float current, float target, float maxDiff) {
        float move = current - target;
        // could also be done with Math.min/max
        if (move < -maxDiff){
            return current - maxDiff;
        } else if (move > maxDiff){
            return current + maxDiff;
        } else {
            return target;
        }
    }

    /**
     * @param source the entity shooting the bullets
     * @param grid the grid where this piece is built on
     * @return a list of projectiles that have been fired at the last call to {@link #update(float, float, boolean, float, float)}
     */
    public List<Projectile> getFiredBullets(Entity source, BlockSubGrid grid){
        List<Projectile> result = new ArrayList<>(storedBullets.size());
        Vector3f thisPos = getStructurePosition(grid);

        for (Pair<Float, Vector3f> bullet : storedBullets) {
            float time = bullet.left;
            Vector3f direction = bullet.right;
            State entityState = source.getStateAt(time);
            Vector3fx spawnPos = new Vector3fx(entityState.position()).add(thisPos);
            direction.rotate(entityState.orientation());

            MutableState projectileSpawnState = new MutableState(time, spawnPos, direction, Toolbox.xTo(direction));
            result.add(getProjectile(projectileSpawnState));
        }
        storedBullets.clear();

        return result;
    }

    /**
     * @param projectileSpawnState the state of the projectile upon firing.
     * @return the projectile produced by this cannon, with the given state.
     */
    protected Projectile getProjectile(MutableState projectileSpawnState) {
        // TODO add projectile type / size / model to json file
        return new DeathIcosahedron(projectileSpawnState);
    }

    @Override
    public PieceTypeBlock getBaseType() {
        return type.bottomPiece;
    }

    @Override
    public AbstractPiece copy() {
        return new GunPiece(position, rotation, color, type);
    }

    @Override
    protected void write(DataOutputStream out, Map<PieceType, Integer> typeMap) throws IOException {
        super.writeToDataStream(out, typeMap);
        Integer typeID = typeMap.computeIfAbsent(type, t -> typeMap.size());
        out.writeInt(typeID);
    }

    public GunPiece(DataInputStream in, PieceType[] typeMap) throws IOException {
        super(in);
        type = (PieceTypeGun) typeMap[in.readInt()];
        timeNextFire = type.reloadTime;
    }

    @Override
    public Shape getShape() {
        return getBaseType().hitbox;
    }
}
