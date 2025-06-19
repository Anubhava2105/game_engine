package components;

import jade.Component;
import jade.GameObject;

public class FontRenderer extends Component {


    @Override
    public void start(){
        if(gameObject.getComponent(SpriteRenderer.class)!=null){
            System.out.println("Found Font renderer");
        }
    }
    @Override
    public void update(float dt){

    }
}
