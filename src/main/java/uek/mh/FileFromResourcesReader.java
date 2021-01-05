package uek.mh;

import java.io.File;

public class FileFromResourcesReader {

    /**
     * @param pathToFileFromResources the location of the file, relative resource folder
     *                                eg. datasets/big/Golden_20.vrp
     */
    public File loadFile(String pathToFileFromResources) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(pathToFileFromResources).getFile());
    }
}
