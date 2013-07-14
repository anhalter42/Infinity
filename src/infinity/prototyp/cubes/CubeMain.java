package infinity.prototyp.cubes;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 07.07.13
 * Time: 17:54
 * To change this template use File | Settings | File Templates.
 */
public class CubeMain {
    private int viewportWidth;
    private int viewportHeight;
    private float rotX;
    private float rotY;
    private float rotZ;
    private float scale = 0.25f;
    private int program;
    private int ticks = 0;
    private ArrayList<Cube> cubes = new ArrayList<Cube>();
    private static final LWJGLTimer timer = new LWJGLTimer();
    private boolean showUI = true;
    private Nifty nifty;
    private int lastUIToggleTick = 0;

    public static void main(String[] args) {
        System.out.println("starting CubeMain prototyp...");
        CubeMain main = new CubeMain();
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
            Display.setTitle("LWJGL Test Cube");
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
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        LwjglInputSystem inputSystem = new LwjglInputSystem();
        LwjglRenderDevice renderDevice = new LwjglRenderDevice();
        inputSystem.startup();
        Logger logger = Logger.getLogger("de.lessvoid.nifty");
        logger.setLevel(Level.WARNING);
        nifty = new Nifty(renderDevice, new NullSoundDevice(), inputSystem, new AccurateTimeProvider());
        String niftyVersion = nifty.getVersion();
        System.out.println("nifty version: " + niftyVersion);

        nifty.fromXml("resources/ui/menu.xml", "welcome");
        //nifty.gotoScreen("start");
        //Screen startscreen = nifty.getScreen("start");
        //System.out.println("bound " + startscreen.isBound());

        program = glCreateProgram();

        if (program == 0) {
            System.err.println("Shader creation failed: Could not find valid memory location in constructor");
            System.exit(1);
        }

        int shader = glCreateShader(GL_VERTEX_SHADER);

        if (shader == 0) {
            System.err.println("Shader creation failed: Could not find valid memory location when adding shader");
            System.exit(1);
        }

        glShaderSource(shader, loadShader("simple.vglsl"));
        glCompileShader(shader);

        if (glGetShader(shader, GL_COMPILE_STATUS) == 0) {
            System.err.println(glGetShaderInfoLog(shader, 1024));
            System.exit(1);
        }

        glAttachShader(program, shader);

        shader = glCreateShader(GL_FRAGMENT_SHADER);

        if (shader == 0) {
            System.err.println("Shader creation failed: Could not find valid memory location when adding shader");
            System.exit(1);
        }

        glShaderSource(shader, loadShader("simple.fglsl"));
        glCompileShader(shader);

        if (glGetShader(shader, GL_COMPILE_STATUS) == 0) {
            System.err.println(glGetShaderInfoLog(shader, 1024));
            System.exit(1);
        }

        glAttachShader(program, shader);
        glLinkProgram(program);

        if (glGetProgram(program, GL_LINK_STATUS) == 0) {
            System.err.println(glGetProgramInfoLog(program, 1024));
            System.exit(1);
        }

        glValidateProgram(program);

        if (glGetProgram(program, GL_VALIDATE_STATUS) == 0) {
            System.err.println(glGetProgramInfoLog(program, 1024));
            System.exit(1);
        }


        initLight();

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
        Matrix4f viewMatrix = createViewMatrix(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
        //viewMatrix.setIdentity();
        System.out.println(viewMatrix.toString());

        Keyboard.enableRepeatEvents(true);

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
            ticks++;
            if (ticks > 120 && nifty.getCurrentScreen().getScreenId().equals("welcome")) {
                nifty.gotoScreen("keys");
            }
            if (ticks > 360 && nifty.getCurrentScreen().getScreenId().equals("keys")) {
                nifty.gotoScreen("menu");
            }
            // render OpenGL here
            GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            //GL11.glClear(GL_DEPTH_BUFFER_BIT);
            //GL11.glClearColor(0,0,0,0);
            //switchToOpenGL();
            glUseProgram(program);

            renderGLScene();
            //glUseProgram(program);
            glUseProgram(0);

            //Display.update();
            //Display.sync(60);

            //switchToNifty();

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
            //glEnable(GL11.GL_TEXTURE_2D);

            if (ticks > 400) {
                if (showUI && !nifty.getCurrentScreen().getScreenId().equals("menu")) {
                    nifty.gotoScreen("menu");
                } else if (!showUI && !nifty.getCurrentScreen().getScreenId().equals("empty")) {
                    nifty.gotoScreen("empty");
                }
            }
            if (nifty.update()) {
                done = true;
            }
            nifty.render(false);
            /*
            if (showUI) {
                if (nifty.update()) {
                    done = true;
                }
                nifty.render(false);
            }
            */

            timer.update();

            processKeyboard((float) timer.getElapsedTime());

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

    private void initCubes() {
        int w = 35;
        int h = 35;
        for (int x = -w; x <= w; x++) {
            for (int z = -h; z <= h; z++) {
                int y = (int) (Math.sin(x / 5.0f) * Math.cos(z / 5.0f) * 5.0f);
                Cube c = new Cube();
                c.position.set(x * scale, y * scale, z * scale);

                if (y < 0) {
                    c.color.y = c.color.x;
                } else {
                    c.color.y = 0.5f + (rnd.nextFloat() * 0.5f);
                    //c.color.y = 0.8f;
                }
                c.scale.set(scale, scale, scale);
                cubes.add(c);
            }
        }
        Cube c = new Cube();
        c.position.set(0.0f, 0.0f, 0.0f);
        c.color.x = 1.0f;
        //c.scale.set(0.25f,0.25f,0.25f);
        cubes.add(c);
        c = new Cube();
        c.position.set(0.0f, 1.0f, 0.0f);
        c.color.z = 1.0f;
        //c.scale.set(0.25f,0.25f,0.25f);
        cubes.add(c);
    }

    private float[] whiteDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] redDiffuse = {1.0f, 0.0f, 0.0f, 1.0f};
    private float[] greenDiffuse = {0.0f, 1.0f, 0.0f, 1.0f};
    private float[] blueDiffuse = {0.0f, 0.0f, 1.0f, 1.0f};
    private float[] posTopLeft = {-2.0f, 2.0f, 0.0f, 1.0f};
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
        renderCubeR(0.0f, 1.0f, -5.0f, 1, 1, 1);
        renderCubeR(0.5f, 2.0f, -5.0f, 0.5f, 0.5f, 1.0f);
        renderCubeR(1.0f, 3.0f, -5.0f, 1.0f, 0.5f, 1.0f);
        renderCubeR(-0.5f, 4.0f, -5.0f, 0.5f, 1.0f, 0.5f);
        renderCubeR(-1.0f, 5.0f, -5.0f, 0.5f, 1.0f, 1.0f);
        for (Cube c : cubes) {
            //GL11.glLoadMatrix(b);
            //glLoadIdentity();
            //glRotatef(pitch, 1, 0, 0);
            //glRotatef(yaw, 0, 1, 0);
            //glRotatef(roll, 0, 0, 1);
            //glTranslatef(-posx, -posy, -posz);
            c.render();
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

    private void renderCube(float x, float y, float z, float r, float g, float b) {
        //GL11.glLoadIdentity();
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glScalef(scale, scale, scale);
        renderCubeI(r, g, b);
        GL11.glPopMatrix();
    }

    private void renderCubeI(float r, float g, float b) {
// Der Würfel aller Würfel ^__^'
        GL11.glColor3f(r, g, b);
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
        exitOnGLError("ERROR: Could not set OpenGL depth testing options");

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        //GL11.glCullFace(GL11.GL_FRONT);
        GL11.glFrontFace(GL11.GL_CCW);
        //GL11.glFrontFace(GL11.GL_CW);
        exitOnGLError("ERROR: Could not set OpenGL culling options");

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

    public void processKeyboard(float delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta " + delta + " is 0 or is smaller than 0");
        }

        boolean keySpeed = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
        boolean keyToggleUI = Keyboard.isKeyDown(Keyboard.KEY_TAB) && !Keyboard.isRepeatEvent();
        boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_W);
        boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_S);
        boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_A);
        boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_D);
        boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
        boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
        boolean keyRotUp = Keyboard.isKeyDown(Keyboard.KEY_UP);
        boolean keyRotDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN);
        boolean keyRotLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT);
        boolean keyRotRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);

        if (keyToggleUI && (ticks-60 > lastUIToggleTick)) {
            if ((showUI && nifty.getCurrentScreen().getScreenId().equals("menu"))
                    || (!showUI && nifty.getCurrentScreen().getScreenId().equals("empty"))) {
                showUI = !showUI;
                lastUIToggleTick = ticks;
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

    public class Cube {
        public Vector3f position = new Vector3f();
        public Vector3f rotation = new Vector3f();
        public Vector3f scale = new Vector3f(1, 1, 1);
        public Vector3f color = new Vector3f();

        public void Cube() {
            color.set(0.5f, 0.5f, 0.5f);
        }

        public void render() {
            GL11.glPushMatrix();
            GL11.glTranslatef(position.x, position.y, position.z);
            GL11.glRotatef(rotation.x, 1.0f, 0.0f, 0.0f);
            GL11.glRotatef(rotation.y, 0.0f, 1.0f, 0.0f);
            GL11.glRotatef(rotation.z, 0.0f, 0.0f, 1.0f);
            GL11.glScalef(scale.x, scale.y, scale.z);
            GL11.glColor3f(color.x, color.y, color.z);
            //GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 100);
            GL11.glBegin(GL11.GL_QUADS);
// Front Side
            GL11.glNormal3f(0.0f, 0.0f, 1.0f);
            GL11.glVertex3f(0.0f, 0.0f, 1.0f);
            GL11.glVertex3f(1.0f, 0.0f, 1.0f);
            GL11.glVertex3f(1.0f, 1.0f, 1.0f);
            GL11.glVertex3f(0.0f, 1.0f, 1.0f);
// Back Side
            GL11.glNormal3f(0.0f, 0.0f, -1.0f);
            GL11.glVertex3f(1.0f, 0.0f, 0.0f);
            GL11.glVertex3f(0.0f, 0.0f, 0.0f);
            GL11.glVertex3f(0.0f, 1.0f, 0.0f);
            GL11.glVertex3f(1.0f, 1.0f, 0.0f);
// Left Side
            GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
            GL11.glVertex3f(0.0f, 0.0f, 0.0f);
            GL11.glVertex3f(0.0f, 0.0f, 1.0f);
            GL11.glVertex3f(0.0f, 1.0f, 1.0f);
            GL11.glVertex3f(0.0f, 1.0f, 0.0f);
// Right Side
            GL11.glNormal3f(1.0f, 0.0f, 0.0f);
            GL11.glVertex3f(1.0f, 0.0f, 1.0f);
            GL11.glVertex3f(1.0f, 0.0f, 0.0f);
            GL11.glVertex3f(1.0f, 1.0f, 0.0f);
            GL11.glVertex3f(1.0f, 1.0f, 1.0f);
// Top Side
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GL11.glVertex3f(0.0f, 1.0f, 1.0f);
            GL11.glVertex3f(1.0f, 1.0f, 1.0f);
            GL11.glVertex3f(1.0f, 1.0f, 0.0f);
            GL11.glVertex3f(0.0f, 1.0f, 0.0f);
// Bottom Side
            GL11.glNormal3f(0.0f, -1.0f, 0.0f);
            GL11.glVertex3f(1.0f, 0.0f, 0.0f);
            GL11.glVertex3f(1.0f, 0.0f, 1.0f);
            GL11.glVertex3f(0.0f, 0.0f, 1.0f);
            GL11.glVertex3f(0.0f, 0.0f, 0.0f);

            GL11.glEnd();
            GL11.glPopMatrix();
        }
    }

    public static class LWJGLTimer {

        private long lastTime; // nanoseconds
        private double elapsedTime;
        private boolean firstRun = true;
        private int FPS = 0;
        private int currentFPS = 0;
        private double elapsedTimeS = 0.0;

        /**
         * Creates a timer.
         */
        public LWJGLTimer() {
        }

        /**
         * Initializes the timer. Call this just before entering the game loop.
         */
        public void initialize() {
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
                return elapsedTime;
            }
        }

        public int getFPS() {
            return FPS;
        }
    }
}
