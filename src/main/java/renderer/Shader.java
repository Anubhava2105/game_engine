package renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private int shaderProgramId;
    private boolean beingUsed=false;
    private String vertexSource;
    private String fragmentSource;
    private String filepath;

    public Shader(String filepath){
        this.filepath = filepath;
        try{
            String source=new String(Files.readAllBytes(Paths.get(filepath)));
            String[] splitString=source.split("(#type)( )+([a-zA-Z]+)");
            //find first pattern
            int index= source.indexOf("#type")+6;
            int eol=source.indexOf("\r\n",index);
            String firstPattern=source.substring(index,eol).trim();
            //find second pattern
            index=source.indexOf("#type",eol)+6;
            eol=source.indexOf("\r\n",index);
            String secondPattern=source.substring(index,eol).trim();
            if(firstPattern.equals("vertex")){
                vertexSource=splitString[1];
            }else if(firstPattern.equals("fragment")){
                fragmentSource=splitString[1];
            }else{
                throw new IOException("Unexpected token '"+firstPattern+"'");
            }
            if(secondPattern.equals("vertex")){
                vertexSource=splitString[2];
            }else if(secondPattern.equals("fragment")){
                fragmentSource=splitString[2];
            }else{
                throw new IOException("Unexpected token '"+secondPattern+"'");
            }
        }
        catch(IOException e){
            e.printStackTrace();
            assert false:"Error: could not open shader file: '"+filepath+ "'";
        }
    }
    public void compile(){
        int vertexId,fragmentId;
        vertexId=glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexId,vertexSource);
        glCompileShader(vertexId);

        int success=glGetShaderi(vertexId,GL_COMPILE_STATUS);
        if(success==GL_FALSE){
            int len=glGetShaderi(vertexId,GL_INFO_LOG_LENGTH);
            System.out.println("Error: '"+filepath+"'\n\t Vertex Shader compilation failed.");
            System.out.println(glGetShaderInfoLog(vertexId,len));
            assert false: " ";
        }
        fragmentId=glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentId,fragmentSource);
        glCompileShader(fragmentId);

        success=glGetShaderi(fragmentId,GL_COMPILE_STATUS);
        if(success==GL_FALSE){
            int len=glGetShaderi(fragmentId,GL_INFO_LOG_LENGTH);
            System.out.println("Error: '"+filepath+"'\n\t Fragment Shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentId,len));
            assert false: " ";
        }

        //Link Shaders
        shaderProgramId=glCreateProgram();
        glAttachShader(shaderProgramId,vertexId);
        glAttachShader(shaderProgramId,fragmentId);
        glLinkProgram(shaderProgramId);
        //check for errors
        success=glGetProgrami(shaderProgramId,GL_LINK_STATUS);
        if(success==GL_FALSE){
            int len=glGetProgrami(shaderProgramId,GL_INFO_LOG_LENGTH);
            System.out.println("Error: '"+filepath+"'\n\t Program Linking failed.");
            System.out.println(glGetProgramInfoLog(shaderProgramId,len));
            assert false: " ";
        }
    }
    public void use(){
        //bind shader program
        if(!beingUsed) {
            glUseProgram(shaderProgramId);
            beingUsed = true;
        }
    }
    public void detach(){
        glUseProgram(0);
        beingUsed=false;
    }

    public void uploadMat4f(String varName, Matrix4f mat4){
        int varLocation=glGetUniformLocation(shaderProgramId,varName);
        if (varLocation == -1) System.err.println("uniform " + varName + " not found");
        use();
        FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);
        mat4.get(mat4Buffer);
        glUniformMatrix4fv(varLocation,false,mat4Buffer);
    }
    public void uploadMat3f(String varName, Matrix3f mat3) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        if (varLocation == -1) System.err.println("uniform " + varName + " not found");
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
        mat3.get(matBuffer);
        glUniformMatrix3fv(varLocation, false, matBuffer);
    }
    public void uploadVec4f(String varName, Vector4f vec){
        int varLocation=glGetUniformLocation(shaderProgramId,varName);
        if (varLocation == -1) System.err.println("uniform " + varName + " not found");
        use();
        glUniform4f(varLocation,vec.x,vec.y,vec.z,vec.w);
    }
    public void uploadVec3f(String varName, Vector3f vec) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        if (varLocation == -1) System.err.println("uniform " + varName + " not found");
        use();
        glUniform3f(varLocation, vec.x, vec.y, vec.z);
    }

    public void uploadVec2f(String varName, Vector2f vec) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        if (varLocation == -1) System.err.println("uniform " + varName + " not found");
        use();
        glUniform2f(varLocation, vec.x, vec.y);
    }
    public void uploadFloat(String varName, float val){
        int varLocation=glGetUniformLocation(shaderProgramId,varName);
        if (varLocation == -1) System.err.println("uniform " + varName + " not found");
        use();
        glUniform1f(varLocation,val);
    }
    public void uploadInt(String varName, int val){
        int varLocation=glGetUniformLocation(shaderProgramId,varName);
        if (varLocation == -1) System.err.println("uniform " + varName + " not found");
        use();
        glUniform1i(varLocation,val);
    }
    public void uploadTexture(String varName, int slot) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        if (varLocation == -1) System.err.println("uniform " + varName + " not found");
        use();
        glUniform1i(varLocation, slot);
    }

    public void uploadIntArray(String varName, int[] array) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        if (varLocation == -1) System.err.println("uniform " + varName + " not found");
        use();
        glUniform1iv(varLocation, array);
    }

    public int getShaderProgramId() {
        return shaderProgramId;
    }
}