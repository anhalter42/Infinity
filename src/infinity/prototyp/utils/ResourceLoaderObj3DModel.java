package infinity.prototyp.utils;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 28.07.13
 * Time: 11:25
 * To change this template use File | Settings | File Templates.
 */
public class ResourceLoaderObj3DModel {

    protected Model3D model;
    protected Model3D.Object3D object3D;
    protected Model3D.Material material;
    protected Model3D.FaceGroup group3D;
    protected boolean isSmoothEnabled = false;
    protected int lineNumber = 0;
    protected Vector3f translation = new Vector3f(0,0,0);
    protected Vector3f scale = new Vector3f(1,1,1);

    private Vector3f parseVertex(String line) {
        String[] xyz = line.split(" ");
        float x = Float.valueOf(xyz[1]) * scale.x + translation.x;
        float y = Float.valueOf(xyz[2]) * scale.y + translation.y;
        float z = Float.valueOf(xyz[3]) * scale.z + translation.z;
        return new Vector3f(x, y, z);
    }

    private static Vector3f parseTextureCoordinate(String line) {
        String[] xyz = line.split(" ");
        float u = Float.valueOf(xyz[1]);
        float v = Float.valueOf(xyz[2]);
        float w = xyz.length > 3 ? Float.valueOf(xyz[3]) : 0;
        return new Vector3f(u, v, w);
    }

    private static Vector3f parseNormal(String line) {
        String[] xyz = line.split(" ");
        float i = Float.valueOf(xyz[1]);
        float j = Float.valueOf(xyz[2]);
        float k = Float.valueOf(xyz[3]);
        return new Vector3f(i, j, k);
    }

    private Model3D.Face parseFace(String line) {
        String[] faceIndices = line.split(" ");
        Model3D.Face face = object3D.newFace();
        face.setMaterial(material);
        String[] l1 = faceIndices[1].split("/");
        String[] l2 = faceIndices[2].split("/");
        String[] l3 = faceIndices.length > 3 ? faceIndices[3].split("/") : null;
        String[] l4 = faceIndices.length > 4 ? faceIndices[4].split("/") : null;
        face.getVertexIndices()[0] = Integer.parseInt(l1[0])-1;
        face.getVertexIndices()[1] = Integer.parseInt(l2[0])-1;
        if (l3 != null) face.getVertexIndices()[2] = Integer.parseInt(l3[0])-1;
        if (l4 != null) face.getVertexIndices()[3] = Integer.parseInt(l4[0])-1;
        if (model.hasTextureCoordinates() && l1.length > 1) {
            face.getTextureCoordinateIndices()[0] = Integer.parseInt(l1[1])-1;
            face.getTextureCoordinateIndices()[1] = Integer.parseInt(l2[1])-1;
            if (l3 != null) face.getTextureCoordinateIndices()[2] = Integer.parseInt(l3[1])-1;
            if (l4 != null) face.getTextureCoordinateIndices()[3] = Integer.parseInt(l4[1])-1;
        }
        if (model.hasNormals() && l1.length > 2) {
            face.getNormalIndices()[0] = Integer.parseInt(l1[2])-1;
            face.getNormalIndices()[1] = Integer.parseInt(l2[2])-1;
            if (l3 != null) face.getNormalIndices()[2] = Integer.parseInt(l3[2])-1;
            if (l4 != null) face.getNormalIndices()[3] = Integer.parseInt(l4[2])-1;
        }
        return face;
    }

    protected Model3D.Object3D acquireObject3D() {
        if (object3D == null) {
            object3D = model.newObject("Dummy");
        }
        return object3D;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3f aTrans) {
        translation = aTrans;
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f aScale) {
        scale = aScale;
    }

    public Model3D loadModel(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        model = new Model3D();
        model.setName(f.getName());
        String line;
        try {
        lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.length() == 0) {
                continue;
            }
            String[] parts = line.split(" ");
            String prefix = parts[0];
            if (prefix.startsWith("#")) {
                continue;
            } else if (prefix.equals("o")) {
                object3D = model.newObject(parts[1]);
                object3D.setSmoothShadingEnabled(isSmoothEnabled);
                group3D = null;
            } else if (prefix.equals("g")) {
                group3D = acquireObject3D().acquireGroup(parts[1]);
            } else if (prefix.equals("v")) {
                model.getVertices().add(parseVertex(line));
            } else if (prefix.equals("vt")) {
                model.getTextureCoordinates().add(parseTextureCoordinate(line));
            } else if (prefix.equals("vn")) {
                model.getNormals().add(parseNormal(line));
            } else if (prefix.equals("mtllib")) {
                for(int i=1;i<parts.length;i++) {
                    loadMaterialLibrary(new File(f.getParentFile().getAbsolutePath() + File.separator + parts[i]));
                }
            } else if (prefix.equals("usemtl")) {
                material = model.getMaterial(parts[1]);
                if (material == null) {
                    throw new RuntimeException("OBJ file '" + f.toString() + "'references unknown material: " + line);
                }
            } else if (prefix.equals("f")) {
                Model3D.Face lFace = parseFace(line);
                lFace.setSmoothShadingEnabled(isSmoothEnabled);
                acquireObject3D().getFaces().add(lFace);
                if (group3D != null) {
                    group3D.getFaces().add(lFace);
                }
            } else if (prefix.equals("s")) {
                isSmoothEnabled = !parts[1].equals("off");
            } else {
                throw new RuntimeException("OBJ file '" + f.toString() + "' contains line #"+lineNumber+" which cannot be parsed correctly: " + line);
            }
        }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        reader.close();
        return model;
    }

    public Model3D loadMaterialLibrary(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        if (model == null) {
            model = new Model3D();
            model.setName(f.getName());
        }
        if (model.getMaterial("None") == null) {
            model.newMaterial("None");
        }
        material = null;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            String[] parts = line.split(" ");
            String prefix = parts[0];
            if (prefix.equals("#")) {
                continue;
            }
            if (prefix.equals("newmtl")) {
                material = model.newMaterial(parts[1]);
            } else if (prefix.equals("Ns")) {
                material.specularCoefficient = Float.valueOf(parts[1]);
            } else if (prefix.equals("Ka")) {
                material.ambientColour.x = Float.valueOf(parts[1]);
                material.ambientColour.y = Float.valueOf(parts[2]);
                material.ambientColour.z = Float.valueOf(parts[3]);
            } else if (prefix.equals("Ks")) {
                material.specularColour.x = Float.valueOf(parts[1]);
                material.specularColour.y = Float.valueOf(parts[2]);
                material.specularColour.z = Float.valueOf(parts[3]);
            } else if (prefix.equals("Kd")) {
                material.diffuseColour.x = Float.valueOf(parts[1]);
                material.diffuseColour.y = Float.valueOf(parts[2]);
                material.diffuseColour.z = Float.valueOf(parts[3]);
            } else if (prefix.equals("Ke")) {
                material.Ke.x = Float.valueOf(parts[1]);
                material.Ke.y = Float.valueOf(parts[2]);
                material.Ke.z = Float.valueOf(parts[3]);
            } else if (prefix.equals("map_Kd")) {
                material.texture = null; // load texture parts[parts.length - 1]
            } else if (prefix.equals("d")) {
                material.d = Float.valueOf(parts[1]);
            } else if (prefix.equals("Ni")) {
                material.Ni = Float.valueOf(parts[1]);
            } else if (prefix.equals("illum")) {
                material.illum = Integer.valueOf(parts[1]);
            } else {
                System.err.println("[MTL] '" + f.toString() + "' Unknown Line: " + line);
            }
        }
        reader.close();
        return model;
    }

    public static void main(String[] args) {
        ResourceLoaderObj3DModel lLoader = new ResourceLoaderObj3DModel();
        try {
            Model3D m = lLoader.loadModel(new File("resources/objects/minion.obj"));
            StringBuilder lBuilder = new StringBuilder();
            m.dump(lBuilder);
            System.out.print(lBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
