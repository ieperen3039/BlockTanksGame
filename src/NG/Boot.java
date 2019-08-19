package NG;

import NG.Core.MainGame;
import NG.Core.Version;
import NG.Tools.Directory;
import NG.Tools.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Boots the Root.
 * <p>
 * The following flags are accepted:
 * <dl>
 * <dt>-debug</dt>
 * <dd>Allow debug output and other features for development</dd>
 * <dt>-quiet</dt>
 * <dd>Only allow error messages, no other output is given.</dd>
 * </dl>
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class Boot {
    private static final Version GAME_VERSION = new Version(0, 1);

    public static void main(String[] argArray) throws Exception {
        List<String> args = new ArrayList<>(Arrays.asList(argArray));

        if (args.contains("-debug")) {
            Logger.setLoggingLevel(Logger.DEBUG);

        } else if (args.contains("-quiet")) {
            Logger.setLoggingLevel(Logger.ERROR);

        } else {
            Logger.setLoggingLevel(Logger.INFO);
        }

        Logger.DEBUG.print("General debug information: "
                // manual aligning will do the trick
                + "\n\tSystem OS:             " + System.getProperty("os.name")
                + "\n\tJava VM:               " + System.getProperty("java.runtime.version")
                + "\n\tGame version:          " + GAME_VERSION
                + "\n\tMain directory         " + Directory.workDirectory()
        );

        new MainGame().root();
    }
}
