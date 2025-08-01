package renderer;

import components.SpriteRenderer;
import jade.Window;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class RenderBatch implements Comparable<RenderBatch>{
    //vertex
    //pos         color       tex coords    texid
    //f,f         f,f,f,f     f,f           f
    private final int POS_SIZE=2;
    private final int COLOR_SIZE=4;
    private final int TEX_COORDS_SIZE=2;
    private final int TEX_ID_SIZE=1;

    private final int POS_OFFSET=0;
    private final int COLOR_OFFSET=POS_OFFSET+POS_SIZE* Float.BYTES;
    private final int TEX_COORDS_OFFSET=COLOR_OFFSET+ COLOR_SIZE*Float.BYTES;
    private final int TEX_ID_OFFSET=TEX_COORDS_OFFSET+ TEX_COORDS_SIZE* Float.BYTES;
    private final int VERTEX_SIZE=9;
    private final int VERTEX_SIZE_BYTES=VERTEX_SIZE* Float.BYTES;

    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertices;
    private int[] texSlots={0,1,2,3,4,5,6,7,};
    private List<Texture> textures;

    private int vaoId,vboId;
    private int maxBatchSize;
    private Shader shader;
    private int zIndex;

    public RenderBatch(int maxBatchSize,int zIndex){
        this.zIndex=zIndex;
//        System.out.println("Render batch");
        shader= AssetPool.getShader("assets/shaders/default.glsl");
        this.sprites=new SpriteRenderer[maxBatchSize];
        this.maxBatchSize=maxBatchSize;

        vertices=new float[maxBatchSize * 4 * VERTEX_SIZE];
        this.numSprites=0;
        this.hasRoom=true;
        this.textures=new ArrayList<Texture>();
    }

    public void start(){
        vaoId=glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId=glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER,vboId);
        glBufferData(GL_ARRAY_BUFFER,  vertices.length * Float.BYTES,GL_DYNAMIC_DRAW);

        int eboId=glGenBuffers();
        int []indices=generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,indices,GL_STATIC_DRAW);

        glVertexAttribPointer(0,POS_SIZE,GL_FLOAT,false,VERTEX_SIZE_BYTES,POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1,COLOR_SIZE,GL_FLOAT,false,VERTEX_SIZE_BYTES,COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2,TEX_COORDS_SIZE,GL_FLOAT,false,VERTEX_SIZE_BYTES,TEX_COORDS_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3,TEX_ID_SIZE,GL_FLOAT,false,VERTEX_SIZE_BYTES,TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);
    }

    public void addSprite(SpriteRenderer sprite){
        int index=this.numSprites;
        this.sprites[index]=sprite;
        this.numSprites++;
        if(sprite.getTexture()!=null){
            if(!textures.contains(sprite.getTexture())){
                textures.add(sprite.getTexture());
            }
        }
        loadVertexProperties(index);
        if(numSprites>=this.maxBatchSize){
            this.hasRoom=false;
        }
    }

    public void render(){
        boolean rebufferData=false;
        for(int i=0;i<numSprites;i++){
            SpriteRenderer sprite=sprites[i];
            if(sprite.isDirty()){
                loadVertexProperties(i);
                sprite.setClean();
                rebufferData=true;
            }
        }
        if(rebufferData){
            glBindBuffer(GL_ARRAY_BUFFER,vboId);
            glBufferSubData(GL_ARRAY_BUFFER,0,vertices);
        }


        //use shader
        shader.use();
        int projectionLocation = glGetUniformLocation(shader.getShaderProgramId(), "uProjection");
        int viewLocation = glGetUniformLocation(shader.getShaderProgramId(), "uView");
        if (projectionLocation == -1 || viewLocation == -1) {
            System.err.println("Failed to locate uniforms in shader");
            return;
        }
        //System.out.println("Shader uniform locations - Projection: " + projectionLocation + ", View: " + viewLocation);

        // Debug prints
        Matrix4f proj = Window.getScene().camera().getProjectionMatrix();
        Matrix4f view = Window.getScene().camera().getViewMatrix();
        shader.uploadMat4f("uProjection", proj);
        shader.uploadMat4f("uView", view);
        for(int i=0;i<textures.size();i++){
            glActiveTexture(GL_TEXTURE0+i+1);
            textures.get(i).bind();
        }
        shader.uploadIntArray("uTextures",texSlots);

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawElements(GL_TRIANGLES, this.numSprites*6 , GL_UNSIGNED_INT, 0);
        //System.out.println("Rendering batch with " + this.numSprites + " sprites");

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);
        for(int i=0;i<textures.size();i++){
            textures.get(i).unbind();
        }

        shader.detach();
    }
    private void loadVertexProperties(int index){
        SpriteRenderer sprite=this.sprites[index];
        int offset=index*4*VERTEX_SIZE;

        Vector4f color=sprite.getColor();
        Vector2f[] texCoords=sprite.getTexCoords();
        int texID=0;
        if(sprite.getTexture()!=null){
            for(int i=0;i<textures.size();i++){
                if(textures.get(i)==sprite.getTexture()){
                    texID=i+1;
                    break;
                }
            }

        }

        float xAdd=1.0f;
        float yAdd=1.0f;
        for(int i=0;i<4;i++){
            if(i==1){
                yAdd=0.0f;
            }else if(i==2){
                xAdd=0.0f;
            }
            else if(i==3){
                yAdd=1.0f;
            }

            vertices[offset]=sprite.gameObject.transform.position.x+(xAdd * sprite.gameObject.transform.scale.x);
            vertices[offset+1]=sprite.gameObject.transform.position.y+(yAdd * sprite.gameObject.transform.scale.y);

            vertices[offset+2]=color.x;
            vertices[offset+3]=color.y;
            vertices[offset+4]=color.z;
            vertices[offset+5]=color.w;

            vertices[offset+6]=texCoords[i].x;
            vertices[offset+7]=texCoords[i].y;

            vertices[offset+8]=texID;

            offset+=VERTEX_SIZE;


        }
        //debugging
//        System.out.println("Loading vertices for sprite " + index + " at position: " +
//                sprite.gameObject.transform.position);

    }
    private int[] generateIndices(){
        int[] elements=new int[6*maxBatchSize];
        for(int i=0;i<maxBatchSize;i++){
            loadElementIndices(elements,i);
        }
        return elements;
    }

    private void loadElementIndices(int[] elements, int index){
        int offsetArrayIndex=6*index;
        int offset=4*index;

        elements[offsetArrayIndex]=offset+3;
        elements[offsetArrayIndex+1]=offset+2;
        elements[offsetArrayIndex+2]=offset;

        elements[offsetArrayIndex+3]=offset;
        elements[offsetArrayIndex+4]=offset+2;
        elements[offsetArrayIndex+5]=offset+1;

    }
    public boolean hasRoom(){
        return this.hasRoom;
    }

    public boolean hasTextureRoom() {
        return this.textures.size() < 8;
    }
    public boolean hasTexture(Texture tex) {
        return this.textures.contains(tex);
    }
    public int zIndex(){
        return this.zIndex;
    }

    @Override
    public int compareTo(@NotNull RenderBatch o) {
        return Integer.compare(o.zIndex(),this.zIndex());
    }
}