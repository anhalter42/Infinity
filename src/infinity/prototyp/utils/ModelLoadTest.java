package infinity.prototyp.utils;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;
import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 07.07.13
 * Time: 17:54
 * To change this template use File | Settings | File Templates.
 */
public class ModelLoadTest {
    private int viewportWidth;
    private int viewportHeight;
    private float rotX;
    private float rotY;
    private float rotZ;
    private float scale = 0.25f;
    private int[] textIds = new int[5];
    private int tindex = 0;
    private int[] programs = new int[5];
    private String[] pnames = new String[programs.length];
    private int pindex = 0;
    private int ticks = 0;
    private ArrayList<Cube> cubes = new ArrayList<Cube>();
    private HashMap<Integer, Cube> opt = new HashMap<Integer, Cube>();
    private static final LWJGLTimer timer = new LWJGLTimer();
    private boolean showUI = true;
    private Nifty nifty;
    private boolean polygonmode = false;
    private float lightdelta = 0.0f;
    private int cubeList = 0;
    private int cubeListL = 0;
    private int cubeListR = 0;
    private int cubeListT = 0;
    private int cubeListD = 0;
    private int cubeListF = 0;
    private int cubeListB = 0;
    private int optI = 0;
    private int cubeListAll = 0;
    private int[] modelList = new int[3];
    private int mindex = 0;
    private boolean showModel = false;
    private int pointCount = 0;
    private int triangleCount = 0;
    private int lineCount = 0;
    private int quadCount = 0;

    public static void main(String[] args) {
        System.out.println("starting ModelLoadTest prototyp...");
        ModelLoadTest main = new ModelLoadTest();
        try {
            main.start();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void start() throws Exception {
        try {
            PixelFormat pixelFormat = new PixelFormat().withDepthBits(24);
            ContextAttribs contextAtrributes = new ContextAttribs(3, 2).withProfileCompatibility(true); // withProfileCore(true);
            //.withForwardCompatible(true);
            //.withProfileCore(true);
            DisplayMode displayMode = new DisplayMode(1024, 768);
            Display.setDisplayMode(displayMode);
            Display.setTitle("LWJGL Test Model Load");
            Display.setInitialBackground(0, 0, 0);
            Display.create(pixelFormat, contextAtrributes);
            System.out.println("openGL version: " + GL11.glGetString(GL11.GL_VERSION));
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        initCubes();

        // init OpenGL here

        IntBuffer viewportBuffer = BufferUtils.createIntBuffer(4 * 4);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuffer);
        viewportWidth = viewportBuffer.get(2);
        viewportHeight = viewportBuffer.get(3);
        GL11.glViewport(0, 0, viewportWidth, viewportHeight);

        GL11.glClearColor(0.4f, 0.4f, 1.0f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        LwjglInputSystem inputSystem = new LwjglInputSystem();
        LwjglRenderDevice renderDevice = new LwjglRenderDevice();
        inputSystem.startup();
        Logger logger = Logger.getLogger("de.lessvoid.nifty");
        logger.setLevel(Level.WARNING);
        nifty = new Nifty(renderDevice, new NullSoundDevice(), inputSystem, new AccurateTimeProvider());
        String niftyVersion = nifty.getVersion();
        System.out.println("nifty version: " + niftyVersion);

        //nifty.fromXml("menu", this.getClass().getResourceAsStream("resources/ui/menu.xml"), "welcome");
        nifty.fromXml("resources/ui/menu.xml", "welcome");
        showUI = false;

        tindex = 0;
        textIds[0] = loadPNGTexture("grass.png", GL13.GL_TEXTURE0);
        textIds[1] = loadPNGTexture("grass_128.png", GL13.GL_TEXTURE0);
        textIds[2] = loadPNGTexture("moss_128.png", GL13.GL_TEXTURE0);
        textIds[3] = loadPNGTexture("ground_stone_128.png", GL13.GL_TEXTURE0);
        textIds[4] = loadPNGTexture("dirt_128.png", GL13.GL_TEXTURE0);

        pindex = 0;
        pnames[0] = "simple";
        pnames[1] = "simple2";
        pnames[2] = "simplet";
        pnames[3] = "simplet2";
        pnames[4] = "simplet3";
        for (String lName : pnames) {
            programs[pindex] = loadProgram(pnames[pindex]);
            pindex++;
        }
        pindex = 0;

        initCubeList();
        initLight();
        initModel();

        NiftyMouse niftyMouse = nifty.getNiftyMouse();

        try {
            // register/load a mouse cursor (this would be done somewhere at the beginning)
            niftyMouse.registerMouseCursor("mouseId", "resources/ui/mousecursor.png", 0, 0);

            // change the cursor to the one we've loaded before
            niftyMouse.enableMouseCursor("mouseId");
        } catch (IOException e) {
            System.err.println("Loading the mouse cursor failed.");
        }
        // we could set the position like so
        niftyMouse.setMousePosition(20, 20);

        Matrix4f perspectiveMatrix = createPerspectiveProjectionMatrix(45.0f, 0.1f, 100.0f);
        System.out.println(perspectiveMatrix.toString());
        //Matrix4f viewMatrix = createViewMatrix(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
        //viewMatrix.setIdentity();
        //System.out.println(viewMatrix.toString());

        Keyboard.enableRepeatEvents(false);

        switchToOpenGL();

        FloatBuffer b = BufferUtils.createFloatBuffer(4 * 4);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        //Matrix4f m = new Matrix4f();
        //Matrix4f.mul(perspectiveMatrix, viewMatrix, m);
        //m.store(b);
        perspectiveMatrix.store(b);
        b.flip();
        GL11.glLoadMatrix(b);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        boolean done = false;
        timer.initialize();
        while (!Display.isCloseRequested() && !done) {
            if (ticks > 120 && nifty.getCurrentScreen().getScreenId().equals("welcome")) {
                nifty.gotoScreen("keys");
            }
            if (ticks > 360 && nifty.getCurrentScreen().getScreenId().equals("keys")) {
                nifty.gotoScreen("menu");
            }
            // render OpenGL here
            GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (polygonmode) {
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL11.GL_LINE);
            } else {
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL11.GL_FILL);
            }
            glUseProgram(programs[pindex]);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textIds[tindex]);
            //GL13.glActiveTexture(GL13.GL_TEXTURE1);
            //glBindTexture(GL_TEXTURE_2D, textIds[1]);

            renderGLScene();

            glUseProgram(0);

            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glLoadIdentity();
            glOrtho(0, viewportWidth, viewportHeight, 0, -32, 32);
            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            glLoadIdentity();

            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            //glEnable(GL_BLEND);
            //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            glEnable(GL11.GL_ALPHA_TEST);
            glAlphaFunc(GL11.GL_NOTEQUAL, 0);
            glDisable(GL11.GL_LIGHTING);
            glEnable(GL11.GL_TEXTURE_2D);

            if (nifty.getCurrentScreen().getScreenId().equals("empty")) {
                Element element = nifty.getCurrentScreen().findElementByName("tFPS");
                element.getRenderer(TextRenderer.class).setText("RunningTime:" + timer.getRuntime() + " FPS: " + timer.getFPS() + " " + (polygonmode ? "P " : " ") + (optimize ? "O" + optI + " " : " ") + " vcount = " + vcount + " prog:" + pnames[pindex] + " texture:" + textIds[tindex]);
            }
            if (ticks > 400) {
                if (showUI && !nifty.getCurrentScreen().getScreenId().equals("menu")) {
                    nifty.gotoScreen("menu");
                } else if (!showUI && !nifty.getCurrentScreen().getScreenId().equals("empty")) {
                    nifty.gotoScreen("empty");
                }
            }
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL11.GL_FILL);

            timer.update();
            ticks += timer.getElapsedTime() / 20; // 1/50s
            processKeyboard((float) timer.getElapsedTime());

            if (nifty.update()) {
                done = true;
            }
            nifty.render(false);

            glDisable(GL_BLEND);
            glDisable(GL_ALPHA_TEST);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
            glEnable(GL_LIGHTING);

            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);

            Display.update();
            Display.sync(60);
            Display.setTitle("LWJGL Test Cube (" + timer.getFPS() + ")");
        }

        Display.destroy();
    }

    private FloatBuffer toBuffer(Vector3f aV) {
        FloatBuffer lBuf = BufferUtils.createFloatBuffer(4);
        lBuf.put(aV.x);
        lBuf.put(aV.y);
        lBuf.put(aV.z);
        lBuf.put(1.0f);
        lBuf.flip();
        return lBuf;
    }

    private String mnames[] = new String[3];

    private void initModel() {
        mnames[0] = "Palme";
        mnames[1] = "Robot_girl";
        mnames[2] = "minion";
        ResourceLoaderObj3DModel lLoader = new ResourceLoaderObj3DModel();
        lLoader.setTranslation(new Vector3f(0.5f, 2.0f, 0.5f));
        //lLoader.setScale(new Vector3f(0.25f,0.25f,0.25f));
        try {
            int li = glGenLists(3);
            modelList[0] = li;
            modelList[1] = li + 1;
            modelList[2] = li + 2;
            Vector3f lDefColor = new Vector3f(0.5f, 0.5f, 0.5f);
            li = 0;
            for (String lName : mnames) {
                pointCount = lineCount = triangleCount = quadCount = 0;
                glNewList(modelList[li], GL_COMPILE);
                li++;
                Model3D lModel = lLoader.loadModel(new File("resources/objects/" + lName + ".obj"));
                for (Model3D.Object3D lObj : lModel.getObjects().values()) {
                    for (Model3D.Face lFace : lObj.getFaces()) {
                        Vector3f lColor = lDefColor;
                        if (lFace.getMaterial() != null) {
                            lColor = lFace.getMaterial().diffuseColour;
                        }
                        switch (lFace.getSize()) {
                            case 1: // Point
                                GL11.glBegin(GL11.GL_POINTS);
                                //glMaterial(GL_FRONT, GL_DIFFUSE, toBuffer(lColor));
                                GL11.glColor3f(lColor.x, lColor.y, lColor.z);
                                //GL11.glPointSize(10.0f);
                                if (lFace.hasNormals()) {
                                    GL11.glNormal3f(lFace.getNormal(lModel, 0).x, lFace.getNormal(lModel, 0).y, lFace.getNormal(lModel, 0).z);
                                }
                                if (lFace.hasTextureCoordinates()) {
                                    GL11.glTexCoord2f(lFace.getTextureCoordinate(lModel, 0).x, lFace.getTextureCoordinate(lModel, 0).y);
                                }
                                GL11.glVertex3f(lFace.getVertex(lModel, 0).x, lFace.getVertex(lModel, 0).y, lFace.getVertex(lModel, 0).z);
                                GL11.glEnd();
                                pointCount++;
                                break;
                            case 2: // Line
                                GL11.glBegin(GL11.GL_LINES);
                                //glMaterial(GL_FRONT, GL_DIFFUSE, toBuffer(lColor));
                                GL11.glColor3f(lColor.x, lColor.y, lColor.z);
                                //GL11.glLineWidth(10.0f);
                                if (lFace.hasNormals()) {
                                    GL11.glNormal3f(lFace.getNormal(lModel, 0).x, lFace.getNormal(lModel, 0).y, lFace.getNormal(lModel, 0).z);
                                }
                                if (lFace.hasTextureCoordinates()) {
                                    GL11.glTexCoord2f(lFace.getTextureCoordinate(lModel, 0).x, lFace.getTextureCoordinate(lModel, 0).y);
                                    GL11.glTexCoord2f(lFace.getTextureCoordinate(lModel, 1).x, lFace.getTextureCoordinate(lModel, 1).y);
                                }
                                GL11.glVertex3f(lFace.getVertex(lModel, 0).x, lFace.getVertex(lModel, 0).y, lFace.getVertex(lModel, 0).z);
                                GL11.glVertex3f(lFace.getVertex(lModel, 1).x, lFace.getVertex(lModel, 1).y, lFace.getVertex(lModel, 1).z);
                                GL11.glEnd();
                                lineCount++;
                                break;
                            case 3: // Triangle
                                GL11.glBegin(GL11.GL_TRIANGLES);
                                //glMaterial(GL_FRONT, GL_DIFFUSE, toBuffer(lColor));
                                GL11.glColor3f(lColor.x, lColor.y, lColor.z);
                                if (lFace.hasNormals()) {
                                    GL11.glNormal3f(lFace.getNormal(lModel, 0).x, lFace.getNormal(lModel, 0).y, lFace.getNormal(lModel, 0).z);
                                }
                                if (lFace.hasTextureCoordinates()) {
                                    GL11.glTexCoord2f(lFace.getTextureCoordinate(lModel, 0).x, lFace.getTextureCoordinate(lModel, 0).y);
                                    GL11.glTexCoord2f(lFace.getTextureCoordinate(lModel, 1).x, lFace.getTextureCoordinate(lModel, 1).y);
                                    GL11.glTexCoord2f(lFace.getTextureCoordinate(lModel, 2).x, lFace.getTextureCoordinate(lModel, 2).y);
                                }
                                GL11.glVertex3f(lFace.getVertex(lModel, 0).x, lFace.getVertex(lModel, 0).y, lFace.getVertex(lModel, 0).z);
                                GL11.glVertex3f(lFace.getVertex(lModel, 1).x, lFace.getVertex(lModel, 1).y, lFace.getVertex(lModel, 1).z);
                                GL11.glVertex3f(lFace.getVertex(lModel, 2).x, lFace.getVertex(lModel, 2).y, lFace.getVertex(lModel, 2).z);
                                GL11.glEnd();
                                triangleCount++;
                                break;
                            case 4: // Quad
                                GL11.glBegin(GL11.GL_QUADS);
                                //glMaterial(GL_FRONT, GL_DIFFUSE, toBuffer(lColor));
                                GL11.glColor3f(lColor.x, lColor.y, lColor.z);
                                if (lFace.hasNormals()) {
                                    GL11.glNormal3f(lFace.getNormal(lModel, 0).x, lFace.getNormal(lModel, 0).y, lFace.getNormal(lModel, 0).z);
                                }
                                if (lFace.hasTextureCoordinates()) {
                                    GL11.glTexCoord2f(lFace.getTextureCoordinate(lModel, 0).x, lFace.getTextureCoordinate(lModel, 0).y);
                                    GL11.glTexCoord2f(lFace.getTextureCoordinate(lModel, 1).x, lFace.getTextureCoordinate(lModel, 1).y);
                                    GL11.glTexCoord2f(lFace.getTextureCoordinate(lModel, 2).x, lFace.getTextureCoordinate(lModel, 2).y);
                                    GL11.glTexCoord2f(lFace.getTextureCoordinate(lModel, 3).x, lFace.getTextureCoordinate(lModel, 3).y);
                                }
                                GL11.glVertex3f(lFace.getVertex(lModel, 0).x, lFace.getVertex(lModel, 0).y, lFace.getVertex(lModel, 0).z);
                                GL11.glVertex3f(lFace.getVertex(lModel, 1).x, lFace.getVertex(lModel, 1).y, lFace.getVertex(lModel, 1).z);
                                GL11.glVertex3f(lFace.getVertex(lModel, 2).x, lFace.getVertex(lModel, 2).y, lFace.getVertex(lModel, 2).z);
                                GL11.glVertex3f(lFace.getVertex(lModel, 3).x, lFace.getVertex(lModel, 3).y, lFace.getVertex(lModel, 3).z);
                                GL11.glEnd();
                                quadCount++;
                                break;
                        }
                    }
                }
                glEndList();
                System.out.println(lName + ": points=" + pointCount + " lines=" + lineCount + " triangles=" + triangleCount + " quads=" + quadCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initCubeList() {
        cubeList = glGenLists(8);
        cubeListL = cubeList + 1;
        cubeListR = cubeList + 2;
        cubeListT = cubeList + 3;
        cubeListD = cubeList + 4;
        cubeListF = cubeList + 5;
        cubeListB = cubeList + 6;
        cubeListAll = cubeList + 7;
        glNewList(cubeList, GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);
// Front Side
        GL11.glNormal3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
// Back Side
        GL11.glNormal3f(0.0f, 0.0f, -1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
// Left Side
        GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
// Right Side
        GL11.glNormal3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
// Top Side
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
// Bottom (Down) Side
        GL11.glNormal3f(0.0f, -1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glEnd();
        GL11.glEndList();
// Front Side
        glNewList(cubeListF, GL_COMPILE);
        GL11.glNormal3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glEndList();
// Back Side
        glNewList(cubeListB, GL_COMPILE);
        GL11.glNormal3f(0.0f, 0.0f, -1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glEndList();
// Left Side
        glNewList(cubeListL, GL_COMPILE);
        GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
        GL11.glEndList();
// Right Side
        glNewList(cubeListR, GL_COMPILE);
        GL11.glNormal3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
        GL11.glEndList();
// Top Side
        glNewList(cubeListT, GL_COMPILE);
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 1.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 1.0f, 0.0f);
        GL11.glEndList();
// Bottom (Down) Side
        glNewList(cubeListD, GL_COMPILE);
        GL11.glNormal3f(0.0f, -1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.0f, 0.0f);
        GL11.glEndList();
        optimize = true;
        glNewList(cubeListAll, GL_COMPILE);
        for (Cube lCube : cubes) {
            lCube.render();
        }
        GL11.glEndList();
        optimize = false;
    }

    private Integer getCubeKey(int x, int y, int z) {
        return x + y * 1000 + z * 1000000;
    }

    private float wheight = 5.0f;

    private void initCubes() {
        int w = 35;
        int h = 35;
        int d = 20;
        for (int x = -w; x <= w; x++) {
            for (int z = -h; z <= h; z++) {
                int yy = (int) (Math.sin(x / wheight) * Math.cos(z / wheight) * wheight);
                for (int y = yy - d; y <= yy; y++) {
                    if (y <= -wheight && (y % 5) == 0) {

                    } else {
                        Cube c = new Cube();
                        c.position.set(x * scale, y * scale, z * scale);

                        if (y < 0) {
                            if (y <= -(wheight - 1)) {
                                c.color.x = 0.3f;
                                c.color.y = 0.3f;
                                c.color.z = 1.0f;
                            } else {
                                c.color.x = ((wheight - y) / wheight);
                                c.color.y = c.color.x * 0.8f;
                                c.color.z = c.color.y * 0.5f;
                            }
                        } else {
                            if (x < 0 && z < 0) {
                                c.color.y = 0.8f;
                            } else if (x > 0 && z < 0) {
                                //c.color.y = 0.5f + (rnd.nextFloat() * 0.5f);
                                c.color.y = (float) (0.5f + Math.abs((Math.sin(x / (wheight * 0.8)) * Math.cos(z / (wheight * 0.8)) * 0.5f * (0.8f + (0.21f * rnd.nextFloat())))));
                                c.color.x = c.color.y;
                            } else {
                                c.color.y = (float) (0.5f + Math.abs((Math.sin(x / (wheight * 0.8)) * Math.cos(z / (wheight * 0.8)) * 0.5f * (0.8f + (0.21f * rnd.nextFloat())))));
                                //c.color.y = 0.5f + (rnd.nextFloat() * 0.5f);
                            }
                        }
                        c.scale.set(scale, scale, scale);
                        cubes.add(c);
                        opt.put(getCubeKey(x, y, z), c);
                    }
                }
            }
        }
        float xx = 0;
        float yy = 0;
        float zz = 0;
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    Cube c = new Cube();
                    c.position.set(xx + x * scale, yy + y * scale, zz + z * scale);
                    c.color.x = 1.0f;
                    c.scale.set(scale, scale, scale);
                    cubes.add(c);
                    opt.put(getCubeKey(x, y, z), c);
                }
            }
        }
        /*
        Cube c = new Cube();
        c.position.set(0.0f, 0.0f, 0.0f);
        c.color.x = 1.0f;
        //c.scale.set(0.25f,0.25f,0.25f);
        cubes.add(c);
        */
        Cube c = new Cube();
        c.position.set(0.0f, 1.0f, 0.0f);
        c.color.z = 1.0f;
        //c.scale.set(0.25f,0.25f,0.25f);
        cubes.add(c);
    }

    private float[] whiteDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] redDiffuse = {1.0f, 0.0f, 0.0f, 1.0f};
    private float[] greenDiffuse = {0.0f, 1.0f, 0.0f, 1.0f};
    private float[] blueDiffuse = {0.0f, 0.0f, 1.0f, 1.0f};
    private float[] posTopLeft = {-20.0f, 50.0f, 0.0f, 1.0f};
    private float[] posTopRight = {2.0f, 2.0f, 0.0f, 1.0f};
    private float[] posBottomFront = {0.0f, -2.0f, 1.0f, 1.0f};

    public void initLight() {
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, arrayToBuffer(whiteDiffuse));
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, arrayToBuffer(posTopLeft));

        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, arrayToBuffer(greenDiffuse));
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, arrayToBuffer(posTopRight));

        GL11.glLight(GL11.GL_LIGHT2, GL11.GL_DIFFUSE, arrayToBuffer(blueDiffuse));
        GL11.glLight(GL11.GL_LIGHT2, GL11.GL_POSITION, arrayToBuffer(posBottomFront));

        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glEnable(GL11.GL_LIGHT1);
        GL11.glEnable(GL11.GL_LIGHT2);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    private FloatBuffer arrayToBuffer(float data[]) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(
                data.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.clear();
        buffer.put(data);
        buffer.rewind();
        return buffer;
    }

    private Random rnd = new Random();

    private float posy = 1.5f;
    private float posx = 0.0f;
    private float posz = 0.0f;
    private float pitch = 0.0f;
    private float yaw = 0.0f;
    private float roll = 0.0f;
    private Matrix4f viewMatrix;

    private void renderGLScene() {
        vcount = 0;
        posTopLeft[0] += lightdelta;
        if (posTopLeft[0] > 150.f) {
            posTopLeft[0] = -150.0f;
        }
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, arrayToBuffer(posTopLeft));

        GL11.glMatrixMode(GL_MODELVIEW);
        /*
        if (1 == 2) {
            viewMatrix = createViewMatrix(posx, posy, posz, 0.0f, 0.0f, 5.0f, 0.0f, -1.0f, 0.0f);
        } else {
            if (viewMatrix == null) {
                viewMatrix = new Matrix4f();
            }
            //float cpitch = (float) Math.cos(pitch);
            //float spitch = (float) Math.sin(pitch);
            viewMatrix.setIdentity();
            viewMatrix.m30 = -posx;
            viewMatrix.m31 = -posy;
            viewMatrix.m32 = -posz;
            //viewMatrix.m11 = cpitch;
            //viewMatrix.m12 = spitch;
            //viewMatrix.m21 = spitch;
            //viewMatrix.m22 = cpitch;
        }
        FloatBuffer b = BufferUtils.createFloatBuffer(4 * 4);
        viewMatrix.store(b);
        b.flip();
        GL11.glLoadMatrix(b);
        */
        glLoadIdentity();
        glRotatef(pitch, 1, 0, 0);
        glRotatef(yaw, 0, 1, 0);
        glRotatef(roll, 0, 0, 1);
        glTranslatef(-posx, -posy, -posz);
        if (mindex >= 0)
            glCallList(modelList[mindex]);
        renderCubeR(0.0f, 1.0f, -5.0f, 1, 1, 1);
        renderCubeR(0.5f, 2.0f, -5.0f, 0.5f, 0.5f, 1.0f);
        renderCubeR(1.0f, 3.0f, -5.0f, 1.0f, 0.5f, 1.0f);
        renderCubeR(-0.5f, 4.0f, -5.0f, 0.5f, 1.0f, 0.5f);
        renderCubeR(-1.0f, 5.0f, -5.0f, 0.5f, 1.0f, 1.0f);
        if (optI == 4) {
            glCallList(cubeListAll);
        } else {
            for (Cube c : cubes) {
                c.render();
            }
        }
        if (rotX < 360) {
            rotX += 0.2f;
        } else {
            rotX = 0.0f;
        }

        if (rotY < 360) {
            rotY += 0.3f;
        } else {
            rotY = 0.0f;
        }

        if (rotZ < 360) {
            rotZ += 0.1f;
        } else {
            rotZ = 0.0f;
        }

        /*
        if (scale >= 1.5f) {
            scaleUp = false;
        } else if (scale <= 0.25f) {
            scaleUp = true;
        }

        if (scaleUp) {
            //scale += 0.0005f;
        } else {
            //scale -= 0.0005f;
        }
        */
    }

    private void renderCubeR(float x, float y, float z, float r, float g, float b) {
        //GL11.glLoadIdentity();
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(rotX, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(rotY, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(rotZ, 0.0f, 0.0f, 1.0f);
        GL11.glScalef(scale, scale, scale);
        renderCubeI(r, g, b);
        GL11.glPopMatrix();
    }

    private void renderCubeI(float r, float g, float b) {
// Der Würfel aller Würfel ^__^'
        GL11.glColor3f(r, g, b);
        GL11.glCallList(cubeList);
        /*
        GL11.glBegin(GL11.GL_QUADS);
// Front Side
        GL11.glNormal3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
// Back Side
        GL11.glNormal3f(0.0f, 0.0f, -1.0f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
// Left Side
        GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
// Right Side
        GL11.glNormal3f(1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
// Top Side
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
// Bottom Side
        GL11.glNormal3f(0.0f, -1.0f, 0.0f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glEnd();
        */
    }

    private void exitOnGLError(String errorMessage) {
        int errorValue = GL11.glGetError();

        if (errorValue != GL11.GL_NO_ERROR) {
            String errorString = GLU.gluErrorString(errorValue);
            System.err.println("ERROR - " + errorMessage + ": " + errorString);

            if (Display.isCreated()) {
                Display.destroy();
            }
            System.exit(-1);
        }
    }

    private void switchToOpenGL() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glEnable(GL_COLOR_MATERIAL);
        exitOnGLError("ERROR: Could not set OpenGL depth testing options");

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        //GL11.glCullFace(GL11.GL_FRONT);
        GL11.glFrontFace(GL11.GL_CCW);
        //GL11.glFrontFace(GL11.GL_CW);
        exitOnGLError("ERROR: Could not set OpenGL culling options");

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        //GL11.glPolygonMode(GL_FRONT_AND_BACK, GL11.GL_LINE);
        exitOnGLError("GL11.glPolygonMode");
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        //GLU.gluPerspective(45.0f, (float) viewportWidth / (float) viewportHeight, 0.1f, 100.0f);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    public static final float RAD_TO_DEG = (float) (180.0f / Math.PI);
    public static final float DEG_TO_RAD = (float) (Math.PI / 180.0f);

    public static Matrix4f createPerspectiveProjectionMatrix(float fov, float zNear, float zFar) {
        Matrix4f m = new Matrix4f();

        float aspectRatio = (float) Display.getWidth() / Display.getHeight();
        float fovY = (float) (2 * Math.atan2(Math.tan(0.5 * fov * DEG_TO_RAD), aspectRatio));

        float f = 1.0f / (float) Math.tan(fovY * 0.5f);


        m.m00 = f / aspectRatio;
        m.m10 = 0;
        m.m20 = 0;
        m.m30 = 0;
        m.m01 = 0;
        m.m11 = f;
        m.m21 = 0;
        m.m31 = 0;
        m.m02 = 0;
        m.m12 = 0;
        m.m22 = (zFar + zNear) / (zNear - zFar);
        m.m32 = (2 * zFar * zNear) / (zNear - zFar);
        m.m03 = 0;
        m.m13 = 0;
        m.m23 = -1;
        m.m33 = 0;

        //m.transpose();

        return m;
    }

    public static Matrix4f createViewMatrix(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        return createViewMatrix(new Vector3f(eyeX, eyeY, eyeZ), new Vector3f(centerX, centerY, centerZ), new Vector3f(upX, upY, upZ));
    }

    public static Matrix4f createViewMatrix(Vector3f eye, Vector3f center, Vector3f up) {
        Matrix4f m = new Matrix4f();

        Vector3f f = new Vector3f();
        Vector3f.sub(center, eye, f);
        //f.sub(center, eye);
        //f.x = center.x - eye.x;
        //f.y = center.y - eye.y;
        //f.z = center.z - eye.z;

        f.normalise();
        up.normalise();

        Vector3f s = new Vector3f();
        Vector3f.cross(f, up, s);
        //s.cross(f, up);
        //s.x = f.x * up.x;
        //s.y = f.y * up.y;
        //s.z = f.z * up.z;
        if (s.length() != 0) {
            s.normalise();
        }

        Vector3f u = new Vector3f();
        Vector3f.cross(s, f, u);
        //u.cross(s, f);
        //u.x = s.x * f.x;
        //u.y = s.y * f.y;
        //u.z = s.z * f.z;
        if (u.length() != 0) {
            u.normalise();
        }

        m.m00 = s.x;
        m.m10 = s.y;
        m.m20 = s.z;
        m.m30 = 0;
        m.m01 = u.x;
        m.m11 = u.y;
        m.m21 = u.z;
        m.m31 = 0;
        m.m02 = -f.x;
        m.m12 = -f.y;
        m.m22 = -f.z;
        m.m32 = 0;
        m.m03 = 0;
        m.m13 = 0;
        m.m23 = 0;
        m.m33 = 1;

        m.m30 = -eye.x;
        m.m31 = -eye.y;
        m.m32 = -eye.z;

        m.transpose();

        return m;
    }

    boolean keySpeed = false;
    boolean keyUp = false;
    boolean keyDown = false;
    boolean keyLeft = false;
    boolean keyRight = false;
    boolean flyUp = false;
    boolean flyDown = false;
    boolean keyRotUp = false;
    boolean keyRotDown = false;
    boolean keyRotLeft = false;
    boolean keyRotRight = false;
    boolean keyRolLeft = false;
    boolean keyRolRight = false;

    public void processKeyboard(float delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta " + delta + " is 0 or is smaller than 0");
        }

        while (Keyboard.next()) {
            int lKey = Keyboard.getEventKey();
            boolean lDown = Keyboard.getEventKeyState();
            //System.out.println("key = " + lKey + " down = " + lDown);
            switch (lKey) {
                case Keyboard.KEY_M:
                    if (lDown) mindex = (mindex + 1) % mnames.length;
                    break;
                case Keyboard.KEY_LCONTROL:
                    keySpeed = lDown;
                    break;
                case Keyboard.KEY_TAB:
                    if (lDown) showUI = !showUI;
                    break;
                case Keyboard.KEY_W:
                    keyUp = lDown;
                    break;
                case Keyboard.KEY_S:
                    keyDown = lDown;
                    break;
                case Keyboard.KEY_A:
                    keyLeft = lDown;
                    break;
                case Keyboard.KEY_D:
                    keyRight = lDown;
                    break;
                case Keyboard.KEY_SPACE:
                    flyUp = lDown;
                    break;
                case Keyboard.KEY_LSHIFT:
                    flyDown = lDown;
                    break;
                case Keyboard.KEY_UP:
                    keyRotUp = lDown;
                    break;
                case Keyboard.KEY_DOWN:
                    keyRotDown = lDown;
                    break;
                case Keyboard.KEY_LEFT:
                    keyRotLeft = lDown;
                    break;
                case Keyboard.KEY_RIGHT:
                    keyRotRight = lDown;
                    break;
                case Keyboard.KEY_Z:
                    keyRolLeft = lDown;
                    break;
                case Keyboard.KEY_C:
                    keyRolRight = lDown;
                    break;
                case Keyboard.KEY_I:
                    if (lDown) pindex = (pindex + 1) % programs.length;
                    break;
                case Keyboard.KEY_T:
                    if (lDown) tindex = (tindex + 1) % textIds.length;
                    break;
                case Keyboard.KEY_P:
                    if (lDown) polygonmode = !polygonmode;
                    break;
                case Keyboard.KEY_R:
                    if (lDown) {
                        posx = posy = posz = yaw = pitch = roll = posTopLeft[0] = 0.0f;
                    }
                    break;
                case Keyboard.KEY_L:
                    if (lDown) if (lightdelta != 0.0) lightdelta = 0.0f;
                    else lightdelta = 0.2f;
                    break;
                case Keyboard.KEY_O:
                    if (lDown) {
                        optI = (optI + 1) % 5;
                        switch (optI) {
                            case 0:
                                optimize3 = optimize2 = optimize = false;
                                break;
                            case 1:
                                optimize3 = optimize2 = false;
                                optimize = true;
                                break;
                            case 2:
                                optimize3 = false;
                                optimize = optimize2 = true;
                                break;
                            case 3:
                                optimize3 = optimize2 = optimize = true;
                                break;
                        }
                    }
                    break;
            }
        }
        if (keySpeed) {
            delta = delta * 4;
        }
        if (keyUp && keyRight && !keyLeft && !keyDown) {
            moveFromLook(delta * 0.003f, 0, -delta * 0.003f);
        }
        if (keyUp && keyLeft && !keyRight && !keyDown) {
            moveFromLook(-delta * 0.003f, 0, -delta * 0.003f);
        }
        if (keyUp && !keyLeft && !keyRight && !keyDown) {
            moveFromLook(0, 0, -delta * 0.003f);
        }
        if (keyDown && keyLeft && !keyRight && !keyUp) {
            moveFromLook(-delta * 0.003f, 0, delta * 0.003f);
        }
        if (keyDown && keyRight && !keyLeft && !keyUp) {
            moveFromLook(delta * 0.003f, 0, delta * 0.003f);
        }
        if (keyDown && !keyUp && !keyLeft && !keyRight) {
            moveFromLook(0, 0, delta * 0.003f);
        }
        if (keyLeft && !keyRight && !keyUp && !keyDown) {
            moveFromLook(-delta * 0.003f, 0, 0);
        }
        if (keyRight && !keyLeft && !keyUp && !keyDown) {
            moveFromLook(delta * 0.003f, 0, 0);
        }
        if (keyRolRight && !keyRolLeft) {
            roll += delta * 0.03f;
        }
        if (!keyRolRight && keyRolLeft) {
            roll -= delta * 0.03f;
        }
        if (keyRotRight && !keyRotLeft) {
            yaw += delta * 0.03f;
        }
        if (!keyRotRight && keyRotLeft) {
            yaw -= delta * 0.03f;
        }
        if (keyRotUp && !keyRotDown) {
            pitch -= delta * 0.03f;
        }
        if (!keyRotUp && keyRotDown) {
            pitch += delta * 0.03f;
        }
        if (flyUp && !flyDown) {
            posy += delta * 0.003f;
        }
        if (flyDown && !flyUp) {
            posy -= delta * 0.003f;
        }
    }

    public void moveFromLook(float dx, float dy, float dz) {
        posz += dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw));
        posx -= dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw));
        posy += dy * (float) sin(toRadians(pitch - 90)) + dz * sin(toRadians(pitch));
        //float hypotenuseX = dx;
        //float adjacentX = hypotenuseX * (float) Math.cos(Math.toRadians(yaw - 90));
        //float oppositeX = (float) Math.sin(Math.toRadians(yaw - 90)) * hypotenuseX;
        //this.z += adjacentX;
        //this.x -= oppositeX;
        //
        //this.y += dy;
        //
        //float hypotenuseZ = dz;
        //float adjacentZ = hypotenuseZ * (float) Math.cos(Math.toRadians(yaw));
        //float oppositeZ = (float) Math.sin(Math.toRadians(yaw)) * hypotenuseZ;
        //this.z += adjacentZ;
        //this.x -= oppositeZ;
    }

    public String loadShader(String fileName) {
        StringBuilder shaderSource = new StringBuilder();
        BufferedReader shaderReader = null;

        try {
            shaderReader = new BufferedReader(new FileReader("resources/shaders/" + fileName));
            String line;

            while ((line = shaderReader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }

            shaderReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }


        return shaderSource.toString();
    }

    public int loadProgram(String fileName) {

        int lprogram = glCreateProgram();

        if (lprogram == 0) {
            System.err.println("Shader creation failed: Could not find valid memory location in constructor");
            System.exit(1);
        }

        int shader = glCreateShader(GL_VERTEX_SHADER);

        if (shader == 0) {
            System.err.println("Shader creation failed: Could not find valid memory location when adding shader");
            System.exit(1);
        }

        glShaderSource(shader, loadShader(fileName + ".vglsl"));
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            System.err.println(glGetShaderInfoLog(shader, 1024));
            System.exit(1);
        }

        glAttachShader(lprogram, shader);

        shader = glCreateShader(GL_FRAGMENT_SHADER);

        if (shader == 0) {
            System.err.println("Shader creation failed: Could not find valid memory location when adding shader");
            System.exit(1);
        }

        glShaderSource(shader, loadShader(fileName + ".fglsl"));
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            System.err.println(glGetShaderInfoLog(shader, 1024));
            System.exit(1);
        }

        glAttachShader(lprogram, shader);
        glLinkProgram(lprogram);

        if (glGetProgrami(lprogram, GL_LINK_STATUS) == 0) {
            System.err.println(glGetProgramInfoLog(lprogram, 1024));
            System.exit(1);
        }

        glValidateProgram(lprogram);

        if (glGetProgrami(lprogram, GL_VALIDATE_STATUS) == 0) {
            System.err.println(glGetProgramInfoLog(lprogram, 1024));
            System.exit(1);
        }
        return lprogram;
    }

    private int loadPNGTexture(String filename, int textureUnit) {
        ByteBuffer buf = null;
        int tWidth = 0;
        int tHeight = 0;

        try {
            // Open the PNG file as an InputStream
            InputStream in = new FileInputStream("resources/images/" + filename);
            // Link the PNG decoder to this stream
            PNGDecoder decoder = new PNGDecoder(in);

            // Get the width and height of the texture
            tWidth = decoder.getWidth();
            tHeight = decoder.getHeight();


            // Decode the PNG file in a ByteBuffer
            buf = ByteBuffer.allocateDirect(
                    4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
            buf.flip();

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Create a new texture object in memory and bind it
        int texId = GL11.glGenTextures();
        GL13.glActiveTexture(textureUnit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

        // All RGB bytes are aligned to each other and each component is 1 byte
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        // Upload the texture data and generate mip maps (for scaling)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, tWidth, tHeight, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        // Setup the ST coordinate system
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        // Setup what to do when the texture has to be scaled
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
                GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
                GL11.GL_LINEAR_MIPMAP_LINEAR);

        exitOnGLError("loadPNGTexture");

        return texId;
    }

    protected boolean optimize = false;
    protected boolean optimize2 = false;
    protected boolean optimize3 = false;
    protected int vcount = 0;
    protected boolean useTriangles = true;


    public class Cube {
        public Vector3f position = new Vector3f();
        public Vector3f rotation = new Vector3f();
        public Vector3f scale = new Vector3f(1, 1, 1);
        public Vector3f color = new Vector3f();

        public void Cube() {
            color.set(0.5f, 0.5f, 0.5f);
        }

        public void render() {
            if (!optimize && !optimize2) {
                GL11.glPushMatrix();
                GL11.glTranslatef(position.x, position.y, position.z);
                GL11.glRotatef(rotation.x, 1.0f, 0.0f, 0.0f);
                GL11.glRotatef(rotation.y, 0.0f, 1.0f, 0.0f);
                GL11.glRotatef(rotation.z, 0.0f, 0.0f, 1.0f);
                GL11.glScalef(scale.x, scale.y, scale.z);
                GL11.glColor3f(color.x, color.y, color.z);
                GL11.glCallList(cubeList);
                GL11.glPopMatrix();
            } else {
                int x = (int) (position.x * 4);
                int y = (int) (position.y * 4);
                int z = (int) (position.z * 4);
                boolean o3 = optimize3;
                boolean o = optimize && scale.x == 0.25;
                boolean f = !o || opt.get(getCubeKey(x, y, z + 1)) == null;
                boolean b = !o || opt.get(getCubeKey(x, y, z - 1)) == null;
                boolean l = !o || opt.get(getCubeKey(x - 1, y, z)) == null;
                boolean r = !o || opt.get(getCubeKey(x + 1, y, z)) == null;
                boolean t = !o || opt.get(getCubeKey(x, y + 1, z)) == null;
                boolean d = !o || opt.get(getCubeKey(x, y - 1, z)) == null;

                boolean vl = posx < position.x;
                boolean vr = posx > (position.x + scale.x);
                if (!vl && !vr) vl = vr = true;
                boolean vd = posy < position.y;
                boolean vt = posy > (position.y + scale.y);
                if (!vt && !vd) vt = vd = true;
                boolean vb = posz < position.z;
                boolean vf = posz > (position.z + scale.z);
                if (!vf && !vb) vf = vb = true;

                if (f || b || l || r || t || d) {
                    if (optimize2) {
                        f = f && vf;
                        b = b && vb;
                        l = l && vl;
                        r = r && vr;
                        t = t && vt;
                        d = d && vd;
                    }
                    GL11.glPushMatrix();
                    GL11.glTranslatef(position.x, position.y, position.z);
                    GL11.glRotatef(rotation.x, 1.0f, 0.0f, 0.0f);
                    GL11.glRotatef(rotation.y, 0.0f, 1.0f, 0.0f);
                    GL11.glRotatef(rotation.z, 0.0f, 0.0f, 1.0f);
                    GL11.glScalef(scale.x, scale.y, scale.z);
                    GL11.glColor3f(color.x, color.y, color.z);
                    //GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, toBuffer(color));
                    if (useTriangles && !o3) {
                        GL11.glBegin(GL11.GL_TRIANGLES);
                    } else {
                        GL11.glBegin(GL11.GL_QUADS);
                    }
// Front Side
                    if (f) {
                        if (o3) {
                            GL11.glCallList(cubeListF);
                        } else {
                            if (useTriangles) {
                                GL11.glNormal3f(0.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 1.0f);

                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 1.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 1.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 1.0f);
                            } else {
                                GL11.glNormal3f(0.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 1.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 1.0f);
                            }
                        }
                        if (useTriangles) {
                            vcount += 6;
                        } else {
                            vcount += 4;
                        }
                    }
// Back Side
                    if (b) {
                        if (o3) {
                            GL11.glCallList(cubeListB);
                        } else {
                            if (useTriangles) {
                                GL11.glNormal3f(0.0f, 0.0f, -1.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 0.0f);

                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 0.0f);
                            } else {
                                GL11.glNormal3f(0.0f, 0.0f, -1.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 0.0f);
                            }
                        }
                        if (useTriangles) {
                            vcount += 6;
                        } else {
                            vcount += 4;
                        }
                    }
// Left Side
                    if (l) {
                        if (o3) {
                            GL11.glCallList(cubeListL);
                        } else {
                            if (useTriangles) {
                                GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 1.0f);

                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 1.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 0.0f);
                            } else {
                                GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 1.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 0.0f);
                            }
                        }
                        if (useTriangles) {
                            vcount += 6;
                        } else {
                            vcount += 4;
                        }
                    }
// Right Side
                    if (r) {
                        if (o3) {
                            GL11.glCallList(cubeListR);
                        } else {
                            if (useTriangles) {
                                GL11.glNormal3f(1.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 0.0f);

                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 1.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 1.0f);
                            } else {
                                GL11.glNormal3f(1.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 1.0f);
                            }
                        }
                        if (useTriangles) {
                            vcount += 6;
                        } else {
                            vcount += 4;
                        }
                    }
// Top Side
                    if (t) {
                        if (o3) {
                            GL11.glCallList(cubeListT);
                        } else {
                            if (useTriangles) {
                                GL11.glNormal3f(0.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 0.0f);

                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 1.0f);
                            } else {
                                GL11.glNormal3f(0.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(1.0f, 1.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 1.0f, 0.0f);
                            }
                        }
                        if (useTriangles) {
                            vcount += 6;
                        } else {
                            vcount += 4;
                        }
                    }
// Bottom (Down) Side
                    if (d) {
                        if (o3) {
                            GL11.glCallList(cubeListD);
                        } else {
                            if (useTriangles) {
                                GL11.glNormal3f(0.0f, -1.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 1.0f);

                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 0.0f);
                            } else {
                                GL11.glNormal3f(0.0f, -1.0f, 0.0f);
                                GL11.glTexCoord2f(0.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 0.0f);
                                GL11.glTexCoord2f(1.0f, 0.0f);
                                GL11.glVertex3f(1.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(1.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 1.0f);
                                GL11.glTexCoord2f(0.0f, 1.0f);
                                GL11.glVertex3f(0.0f, 0.0f, 0.0f);
                            }
                        }
                        if (useTriangles) {
                            vcount += 6;
                        } else {
                            vcount += 4;
                        }
                    }

                    GL11.glEnd();
                    GL11.glPopMatrix();
                }
            }
        }
    }

    public static class LWJGLTimer {

        private long firstTime; // nanoseconds
        private long lastTime; // nanoseconds
        private double elapsedTime;
        private boolean firstRun = true;
        private int FPS = 0;
        private int currentFPS = 0;
        private double elapsedTimeS = 0.0;
        private long durationMS = 0;

        /**
         * Creates a timer.
         */
        public LWJGLTimer() {
        }

        /**
         * Initializes the timer. Call this just before entering the game loop.
         */
        public void initialize() {
            firstTime = System.nanoTime();
            lastTime = System.nanoTime();
            firstRun = false;
        }

        /**
         * @return the elapsed time since the the next to last update call
         */
        public double getElapsedTime() {
            return elapsedTime;
        }

        /**
         * Updates the timer. Call this once every iteration of the game loop. The first time you call this method it
         * returns 0.
         *
         * @return the elapsed time in milliseconds
         */
        public double update() {
            long now = System.nanoTime();
            currentFPS++;
            if (elapsedTimeS >= 1000.0) {
                //System.out.println("FPS: " + FPS + " ET: " + elapsedTimeS);
                FPS = currentFPS;
                currentFPS = 0;
                elapsedTimeS = 0.0;
            }
            if (firstRun) {
                firstRun = false;
                lastTime = now;
                return 0;
            } else {
                long lElapsedTime = System.nanoTime() - lastTime;
                lastTime = now;
                elapsedTime = lElapsedTime / (double) 1000000;
                elapsedTimeS += elapsedTime;
                // running time
                double ldurationMS = (lastTime - firstTime) / 1000000; ///1e9;
                durationMS = (long) ldurationMS;
                return elapsedTime;
            }
        }

        public int getFPS() {
            return FPS;
        }

        public String getRuntime() {
            return new SimpleDateFormat("HH:mm:ss").format(new Date(durationMS));
        }
    }
}
