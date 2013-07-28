package infinity.prototyp.utils;

import org.lwjgl.util.vector.Vector3f;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 28.07.13
 * Time: 11:25
 * To change this template use File | Settings | File Templates.
 */
public class ResourceLoaderObj3DModel {

    private static Vector3f parseVertex(String line) {
        String[] xyz = line.split(" ");
        float x = Float.valueOf(xyz[1]);
        float y = Float.valueOf(xyz[2]);
        float z = Float.valueOf(xyz[3]);
        return new Vector3f(x, y, z);
    }

    private static Vector3f parseNormal(String line) {
        String[] xyz = line.split(" ");
        float x = Float.valueOf(xyz[1]);
        float y = Float.valueOf(xyz[2]);
        float z = Float.valueOf(xyz[3]);
        return new Vector3f(x, y, z);
    }

    private static Model3D.Face parseFace(boolean hasNormals, String line) {
        String[] faceIndices = line.split(" ");
        int[] vertexIndicesArray = {Integer.parseInt(faceIndices[1].split("/")[0]),
                Integer.parseInt(faceIndices[2].split("/")[0]), Integer.parseInt(faceIndices[3].split("/")[0])};
        if (hasNormals) {
            int[] normalIndicesArray = new int[3];
            normalIndicesArray[0] = Integer.parseInt(faceIndices[1].split("/")[2]);
            normalIndicesArray[1] = Integer.parseInt(faceIndices[2].split("/")[2]);
            normalIndicesArray[2] = Integer.parseInt(faceIndices[3].split("/")[2]);
            return new Model3D.Face(vertexIndicesArray, normalIndicesArray);
        } else {
            return new Model3D.Face((vertexIndicesArray));
        }
    }

    public static Model3D loadModel(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        Model3D m = new Model3D();
        Model3D.Object3D o = null;
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ");
            String prefix = parts[0];
            if (prefix.equals("#")) {
                continue;
            } else if (prefix.equals("o")) {
                o = m.newObject(parts[1]);
            } else if (prefix.equals("v")) {
                o.getVertices().add(parseVertex(line));
            } else if (prefix.equals("vn")) {
                o.getNormals().add(parseNormal(line));
            } else if (prefix.equals("f")) {
                o.getFaces().add(parseFace(o.hasNormals(), line));
            } else {
                throw new RuntimeException("OBJ file contains line which cannot be parsed correctly: " + line);
            }
        }
        reader.close();
        return m;
    }
}
