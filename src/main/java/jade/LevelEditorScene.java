package jade;


import components.SpriteRenderer;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.AssetPool;

public class LevelEditorScene extends Scene{


    public LevelEditorScene(){


    }
    @Override
    public void init(){

        this.camera=new Camera(new Vector2f());
        int xOffset=100,yOffset=100;
        float totalWidth=(float)(600-xOffset*2);
        float totalHeight=(float)(300-yOffset*2);
        float sizeX=totalWidth/10.0f;
        float sizeY=totalHeight/10.0f;
        for(int x=0;x<100;x++){
            for(int y=0;y<100;y++){
                float xPos=xOffset+(x*sizeX);
                float yPos=yOffset+(y*sizeY);

                GameObject go=new GameObject("Obj"+x+" "+y, new Transform(new Vector2f(xPos,yPos),new Vector2f(sizeX,sizeY)));
                go.addComponent(new SpriteRenderer(new Vector4f(0,0,0,0)));
                this.addGameObjectToScene(go);
            }
        }
        loadResources();

    }
    private void loadResources(){
        AssetPool.getShader("assets/shaders/default.glsl");

    }
    @Override
    public void update(float dt){
//        System.out.println("FPS: "+ 1.0f/dt);
        for(GameObject go:gameObjects){
            go.update(dt);
        }
        this.renderer.render();

    }
}
