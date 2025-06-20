package jade;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import util.Time;

import java.nio.*;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;
public class Window {
    private final int width;
    private final int height;
    private final String title;
    private long glfwWindow;
    public float r,g,b,a;
    //    private boolean fadeToBlack=false;
    private static Window window=null;
    private static Scene currentScene=null;
    private Window(){
        this.width=1920;
        this.height=1080;
        this.title="Mario";
        this.r=1.0f;
        this.g=1.0f;
        this.b=1.0f;
        this.a=1.0f;
    }
    public static void changeScene(int newScene){
        switch(newScene){
            case 0:
                currentScene=new LevelEditorScene();
                currentScene.init();
                currentScene.start();
                break;
            case 1:
                currentScene=new LevelScene();
                currentScene.init();
                currentScene.start();
                break;
            default:
                assert false: "Unknown Scene '"+newScene+"'";
                break;
        }
    }

    public static Window get(){
        if(Window.window==null){
            Window.window=new Window();
        }
        return Window.window;
    }
    public static Scene getScene(){
        return get().currentScene;
    }
    public void run(){
        System.out.println("Hello LWJGL"+ Version.getVersion()+"!");
        init();
        loop();
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
    public void init(){
        //Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_MAXIMIZED,GLFW_TRUE);//the window will be maximised

        // Create the window
        glfwWindow = glfwCreateWindow(this.width,this.height,this.title,NULL,NULL);
        if ( glfwWindow == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetCursorPosCallback(glfwWindow,MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow,MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow,MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow,KeyListener::keyCallback);
       //make the OPENGL context current
        glfwMakeContextCurrent(glfwWindow);
        //enable v-sync
        glfwSwapInterval(1);

//        // Get the thread stack and push a new frame
//        try ( MemoryStack stack = stackPush() ) {
//            IntBuffer pWidth = stack.mallocInt(1); // int*
//            IntBuffer pHeight = stack.mallocInt(1); // int*
//
//            // Get the window size passed to glfwCreateWindow
//            glfwGetWindowSize(window, pWidth, pHeight);
//
//            // Get the resolution of the primary monitor
//            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//
//            // Center the window
//            glfwSetWindowPos(
//                    window,
//                    (vidmode.width() - pWidth.get(0)) / 2,
//                    (vidmode.height() - pHeight.get(0)) / 2
//            );
//        } // the stack frame is popped automatically

        // Make the window visible
        glfwShowWindow(glfwWindow);
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        Window.changeScene(0);
    }
    public void loop(){

        float beginTime= Time.getTime();
        float endTime= Time.getTime();
        float dt=-1.0f;

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(glfwWindow) ) {
            //Poll events
            glfwPollEvents();
            glClearColor(r,g,b,a);
            glClear(GL_COLOR_BUFFER_BIT);

            if(dt>=0) currentScene.update(dt);
            glfwSwapBuffers(glfwWindow);
            endTime=Time.getTime();
            dt=endTime-beginTime;
            beginTime=endTime;
        }
    }
}
