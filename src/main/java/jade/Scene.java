package jade;

import renderer.RenderBatch;
import renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

public abstract class Scene {
    protected Renderer renderer=new Renderer();
    protected Camera camera;
    private boolean isRunning=false;
    protected List<GameObject> gameObjects=new ArrayList<GameObject>();
    public Scene(){

    }
    public void start(){
        for(GameObject go:gameObjects){
            go.start();
            this.renderer.add(go);
        }
        isRunning=true;
    }
    public void addGameObjectToScene(GameObject go){
        if(!isRunning){
            gameObjects.add(go);
        }
        else{
            gameObjects.add(go);
            go.start();
            this.renderer.add(go);
        }
    }
    public void init(){

    }
    public abstract void update(float dt);

    public Camera camera(){
        return this.camera;
    }
}
