package infinity.prototyp.utils;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 28.07.13
 * Time: 11:26
 * To change this template use File | Settings | File Templates.
 */
public class Model3D {
    private final HashMap<String, Material> materials = new HashMap<String, Material>();
    private final HashMap<String, Object3D> objects = new HashMap<String, Object3D>();
    private final List<Vector3f> vertices = new ArrayList<Vector3f>();
    private final List<Vector3f> textureCoordinates = new ArrayList<Vector3f>();
    private final List<Vector3f> normals = new ArrayList<Vector3f>();
    private String name;

    public HashMap<String, Material> getMaterials() {
        return materials;
    }

    public Material getMaterial(String aName) {
        return materials.get(aName);
    }

    public Material newMaterial(String aName) {
        Material lMaterial = new Material();
        lMaterial.name = aName;
        getMaterials().put(lMaterial.name, lMaterial);
        return lMaterial;
    }

    public HashMap<String, Object3D> getObjects() {
        return objects;
    }

    public Object3D getObject(String aName) {
        return objects.get(aName);
    }

    public Object3D newObject(String aName) {
        Object3D lObject = new Object3D();
        lObject.name = aName;
        getObjects().put(lObject.name, lObject);
        return lObject;
    }

    public boolean hasTextureCoordinates() {
        return getTextureCoordinates().size() > 0;
    }

    public boolean hasNormals() {
        return getNormals().size() > 0;
    }

    public List<Vector3f> getVertices() {
        return vertices;
    }

    public List<Vector3f> getTextureCoordinates() {
        return textureCoordinates;
    }

    public List<Vector3f> getNormals() {
        return normals;
    }

    @Override
    public String toString() {
        return "Model3D{objects=" + getObjects().size() + ",materials=" + getMaterials().size() + ",vertices=" + getVertices().size() + ",normals=" + getNormals().size() + ",textures=" + getTextureCoordinates().size() + '}';
    }

    public void dump(StringBuilder aBuilder) {
        aBuilder.append("model=");
        aBuilder.append('\'');
        aBuilder.append(name);
        aBuilder.append('\'');
        aBuilder.append("objects={\n");
        for(Object3D lObj : objects.values()) {
            lObj.dump(this, aBuilder);
        }
        aBuilder.append("}\n");
    }

    public void setName(String aName) {
        name = aName;
    }

    public String getName() {
        return name;
    }

    public static class Texture {

    }

    public static class Material {

        @Override
        public String toString() {
            return "Material{" +
                    "name='" + name + "'" +
                    ",specularCoefficient=" + specularCoefficient +
                    ", ambientColour=" + ambientColour +
                    ", diffuseColour=" + diffuseColour +
                    ", specularColour=" + specularColour +
                    '}';
        }

        public String name;
        /** Between 0 and 1000. */
        public float specularCoefficient = 100;
        public Vector3f ambientColour = new Vector3f(0.2f, 0.2f, 0.2f);
        public Vector3f diffuseColour = new Vector3f(0.3f, 1, 1);
        public Vector3f specularColour = new Vector3f(1, 1, 1);
        public Texture texture;
        public int illum = -1;
        public float d;
        public float Ni;
    }

    public static class Face {

        private final int[] vertexIndices = {-1, -1, -1};
        private final int[] normalIndices = {-1, -1, -1};
        private final int[] textureCoordinateIndices = {-1, -1, -1};
        private Material material;

        public Material getMaterial() {
            return material;
        }

        public boolean hasNormals() {
            return normalIndices[0] != -1;
        }

        public boolean hasTextureCoordinates() {
            return textureCoordinateIndices[0] != -1;
        }

        public int[] getVertexIndices() {
            return vertexIndices;
        }

        public int[] getTextureCoordinateIndices() {
            return textureCoordinateIndices;
        }

        public int[] getNormalIndices() {
            return normalIndices;
        }

        public Face() {
        }

        public Face(int[] vertexIndices) {
            this.vertexIndices[0] = vertexIndices[0];
            this.vertexIndices[1] = vertexIndices[1];
            this.vertexIndices[2] = vertexIndices[2];
        }

        public Face(int[] vertexIndices, int[] normalIndices) {
            this.vertexIndices[0] = vertexIndices[0];
            this.vertexIndices[1] = vertexIndices[1];
            this.vertexIndices[2] = vertexIndices[2];
            this.normalIndices[0] = normalIndices[0];
            this.normalIndices[1] = normalIndices[1];
            this.normalIndices[2] = normalIndices[2];
        }

        public Face(int[] vertexIndices, int[] normalIndices, int[] textureCoordinateIndices, Material material) {
            this.vertexIndices[0] = vertexIndices[0];
            this.vertexIndices[1] = vertexIndices[1];
            this.vertexIndices[2] = vertexIndices[2];
            this.textureCoordinateIndices[0] = textureCoordinateIndices[0];
            this.textureCoordinateIndices[1] = textureCoordinateIndices[1];
            this.textureCoordinateIndices[2] = textureCoordinateIndices[2];
            this.normalIndices[0] = normalIndices[0];
            this.normalIndices[1] = normalIndices[1];
            this.normalIndices[2] = normalIndices[2];
            this.material = material;
        }

        @Override
        public String toString() {
            return "Face{vertices=" + getVertexIndices().length + ",normals=" + getNormalIndices().length + ",textures=" + getTextureCoordinateIndices().length + '}';
        }

        public void dump(Model3D aModel, StringBuilder aBuilder) {
            if (material != null) {
                aBuilder.append(material.toString());
                aBuilder.append("\n");
            }
            aBuilder.append("vertices={");
            for(int i : vertexIndices) {
                aBuilder.append(", ");
                aBuilder.append(aModel.getVertices().get(i).toString());
            }
            aBuilder.append("}\nnormals={");
            if (hasNormals()) {
                for(int i : normalIndices) {
                    aBuilder.append(", ");
                    aBuilder.append(aModel.getNormals().get(i).toString());
                }
            }
            aBuilder.append("}\ntextures={");
            if (hasTextureCoordinates()) {
                for(int i : textureCoordinateIndices) {
                    aBuilder.append(", ");
                    aBuilder.append(aModel.getTextureCoordinates().get(i).toString());
                }
            }
            aBuilder.append("}\n");
        }

        public void setMaterial(Material aMaterial) {
            material = aMaterial;
        }
    }

    public static class Object3D {
        private String name;
        private final List<Face> faces = new ArrayList<Face>();
        private boolean enableSmoothShading = true;

        public boolean isSmoothShadingEnabled() {
            return enableSmoothShading;
        }

        public void setSmoothShadingEnabled(boolean smoothShadingEnabled) {
            this.enableSmoothShading = smoothShadingEnabled;
        }

        public List<Face> getFaces() {
            return faces;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Object3D{name='" + name + "' ,faces=" + getFaces().size() + " ,smooth=" + isSmoothShadingEnabled() + '}';
        }

        public Face newFace() {
            return new Face();
        }

        public void dump(Model3D aModel, StringBuilder aBuilder) {
            aBuilder.append('\'');
            aBuilder.append(name);
            aBuilder.append('\'');
            aBuilder.append("\n");
            aBuilder.append("faces={");
            for(Face lFace : faces) {
                lFace.dump(aModel, aBuilder);
            }
            aBuilder.append("}\n");
        }
    }
}
