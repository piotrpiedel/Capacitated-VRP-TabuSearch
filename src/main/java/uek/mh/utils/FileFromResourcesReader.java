package uek.mh.utils;

import java.io.*;

public class FileFromResourcesReader {

    /**
     * @param pathToFileFromResources the location of the file, relative resource folder
     *                                eg. Mhprojekt.vrp
     * @return
     */
    public BufferedReader loadFile(String pathToFileFromResources) throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(pathToFileFromResources).getFile());
        if (file.exists()) {
            return new BufferedReader(new FileReader(file));
        }
        if (!file.exists()) {
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(pathToFileFromResources);
            assert resourceAsStream != null;
            Reader reader = new InputStreamReader(resourceAsStream);
            return new BufferedReader(reader);

        }
        throw new FileNotFoundException();
    }
}
