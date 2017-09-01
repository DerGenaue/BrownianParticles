package de.dergenaue.brownianparticles;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.opengl.GLES11;
import android.opengl.GLUtils;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * Created by Daniel on 07.03.2016.
 */
public class BrownianParticlesRenderer implements GLWallpaperService.Renderer {
    private final static int WAVE_DUR = 500, WAVE_MAX = WAVE_DUR / 2;

    private final Particle[] particles;

    private float[] pPos, pSize, pColor;
    private final FloatBuffer pPBuffer, pSBuffer, pCBuffer;

    private int width, height, waveTime;
    private long waveStart = 0;

    private int tex = 0, touchMode = 0, maxSize = 80; // 50 is Maximum ?
    public float colorR, colorG, colorB,
            colorWR, colorWG, colorWB,
            touchX, touchY;

    public BrownianParticlesRenderer(){
        prepareParticle();
        setColor(Color.BLACK);
        setWColor(Color.BLACK);

        particles = new Particle[271];

        for(int i = 0; i < particles.length; i++)
            particles[i] = new Particle();

        pPos = new float[particles.length * 2];
        pSize = new float[particles.length];
        pColor = new float[particles.length * 4];

        pPBuffer = ByteBuffer.allocateDirect(pPos.length *4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pSBuffer = ByteBuffer.allocateDirect(pSize.length *4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pCBuffer = ByteBuffer.allocateDirect(pColor.length *4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public void setColor(int color){
        colorR = (float)Color.red(color)/255;
        colorG = (float)Color.green(color)/255;
        colorB = (float)Color.blue(color)/255;
    }
    public void setWColor(int color){
        colorWR = (float)Color.red(color)/255;
        colorWG = (float)Color.green(color)/255;
        colorWB = (float)Color.blue(color)/255;
    }

    public void touchAt(float x, float y, int mode){
        touchX = x*2-width;
        touchY = height-2*y;
        touchMode = mode;
        if(mode == 1)
            waveStart = System.currentTimeMillis();
    }


    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL11.GL_COLOR_BUFFER_BIT);

        calcFrame();

        pPBuffer.put(pPos).position(0);
        pSBuffer.put(pSize).position(0);
        pCBuffer.put(pColor).position(0);

        gl.glDrawArrays(GL11.GL_POINTS, 0, particles.length);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glEnable(GL11.GL_TEXTURE);
        gl.glEnable(GL11.GL_TEXTURE_2D);

        int[] texId = new int[1];
        GLES11.glGenTextures(1, texId, 0);
        tex = texId[0];
        gl.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GLUtils.texImage2D(GL11.GL_TEXTURE_2D, 0, particle, 0);


        gl.glEnable(GL11.GL_BLEND);
        gl.glEnable(GL11.GL_POINT_SPRITE_OES);
        gl.glDisable(GL11.GL_DEPTH_TEST);
        gl.glDepthMask(false);
        gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        gl.glTexEnvf(GL11.GL_POINT_SPRITE_OES, GL11.GL_COORD_REPLACE_OES, GL11.GL_TRUE);

        gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnableClientState(GL11.GL_POINT_SIZE_ARRAY_BUFFER_BINDING_OES); // Not necessary ?
        gl.glEnableClientState(GL11.GL_POINT_SIZE_ARRAY_OES);
        gl.glEnableClientState(GL11.GL_POINT_SPRITE_OES);
        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL11.GL_COLOR_ARRAY);

        gl.glVertexPointer(2, GL11.GL_FLOAT, 0, pPBuffer);
        ((GL11)gl).glPointSizePointerOES(GL11.GL_FLOAT, 0, pSBuffer);
        gl.glColorPointer(4, GL11.GL_FLOAT, 0, pCBuffer);

        gl.glColor4f(0,1,0,1);
        gl.glClearColor(0, 0, 0, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
    }


    public void release(){
    }


    // When Scrolling on home screen
    float xOffOld = -1, dXOff = 0,
            dxFactor = 2;
    public void onOffsetsChanged(float xOffset, float yOffset,
                                 float xStep, float yStep, int xPixels, int yPixels){
        if(xOffOld >= 0)
            dXOff += xOffset - xOffOld;
        xOffOld = xOffset;
    }



    private Bitmap particle = null;
    private int phh = 40;


    private void prepareParticle(){
        particle = Bitmap.createBitmap(2*phh, 2*phh, Bitmap.Config.ALPHA_8);
        Canvas c = new Canvas(particle);
        Paint p = new Paint();
        //p.setColor(Color.WHITE);
        p.setShader(new RadialGradient(0, 0, phh,
                new int[]{Color.argb(245, 255, 255, 255), Color.argb(180, 255, 255, 255), Color.argb(0, 255, 255, 255)},
                new float[]{0, 0.7f, 1}, Shader.TileMode.CLAMP));
        c.save();
        c.translate(phh, phh);
        c.drawRect(-phh, -phh, phh, phh, p);
        c.restore();
        //c.drawRect(0, 0, 10, 10, p);
        //c.drawRect(20, 20, 30, 30, p);
    }




    float jitter = 0.01f,
            friction = 0.5f,
            frictioZ = friction*0.1f,
            jitter2 = jitter*0.5f,
            sMin = 0.1f,
            sCalc = 1-sMin,
            sMin1 = 1-sMin*sMin*10,
            pS, tmp, randC0 = 0, randC1 = 0, randC2 = 0, waveMove, deact;
    private float[] rotMat = new float[]{
            0, 0, 0,
            0, 0, 0,
            0, 0, 0};
    private Particle curPart;
    private int i, i2, i4, ii = 0;
    private Random rand = new Random();
    private void calcFrame(){
        if(dXOff != 0) {
            rotMat[0] = (float) Math.cos(dXOff*dxFactor);
            rotMat[1] = 0;
            rotMat[2] = (float) Math.sin(dXOff*dxFactor);
            rotMat[3] = 0;
            rotMat[4] = 1;
            rotMat[5] = 0;
            rotMat[6] = (float) -Math.sin(dXOff*dxFactor);
            rotMat[7] = 0;
            rotMat[8] = (float) Math.cos(dXOff*dxFactor);
        }
        waveTime = (int) Math.min(System.currentTimeMillis() - waveStart, Integer.MAX_VALUE);
        waveMove = (WAVE_MAX-waveTime) * 0.01f;
        for(i = i2 = i4 = 0; i < particles.length; i++){
            curPart = particles[i];

            if(dXOff != 0) { // Rotate Particle
                float x = curPart.X, y = curPart.Y, z = Math.abs(curPart.Z)*2-1, zs = Math.signum(curPart.Z); // Rotate around absolute value of Z-Achsis
                curPart.X = rotMat[0] * x + rotMat[1] * y + rotMat[2] * z;
                curPart.Y = rotMat[3] * x + rotMat[4] * y + rotMat[5] * z;
                curPart.Z = rotMat[6] * x + rotMat[7] * y + rotMat[8] * z + 1;
                curPart.Z *= 0.5 * zs;
            }

            curPart.X += curPart.vX;
            curPart.Y += curPart.vY;
            curPart.Z += curPart.vZ;

            curPart.vX = curPart.vX * friction + randC0;
            curPart.vY = curPart.vY * friction + randC1;
            curPart.vZ = curPart.vZ * frictioZ + randC2;

            tmp = randC0;
            randC0 = randC1;
            randC1 = randC2;
            randC2 = tmp;
            if((i+ii) % 7 == 0) // Yes, calling rand.nextFloat() only every so-often gives us a valid performance increase!
                randC2 = rand.nextFloat() * jitter - jitter2;

            if(curPart.Z > 1 || curPart.Z < -1) // No call to Math.abs
                curPart.vZ = -0.5f * Math.signum(curPart.Z) * Math.abs(curPart.vZ);


            if(curPart.X < -1)
                curPart.vX = Math.abs(curPart.vX);
            if(curPart.X > 1)
                curPart.vX = - Math.abs(curPart.vX);
            if(curPart.Y < -1)
                curPart.vY = Math.abs(curPart.vY);
            if(curPart.Y > 1)
                curPart.vY = - Math.abs(curPart.vY);

            curPart.activation *= 0.98;
            deact = 1-curPart.activation;

            // Graphics
            pS = curPart.Z*curPart.Z*sCalc + sMin;


            pPos[i2++] = curPart.X;
            pPos[i2++] = curPart.Y;

            pSize[i] = pS * maxSize;


            if(waveTime < WAVE_DUR || touchMode == 2) {
                float distX = curPart.X*width - touchX,
                        distY = curPart.Y*height - touchY,
                        dist = distX*distX + distY*distY,
                        ddist = dist - (waveTime*waveTime*10);
                if(touchMode== 2) {
                    curPart.activation = Math.max(curPart.activation, 1 / (1 + dist * 0.00001f));
                }else{
                    curPart.vX += distX / dist * waveMove;
                    curPart.vY += distY / dist * waveMove;
                    if (ddist < 50 && ddist > -500000)
                        curPart.activation = Math.max(curPart.activation, 1 / (1 + dist * 0.000001f));
                }
            }
            pColor[i4++] = colorR*deact + colorWR*curPart.activation;
            pColor[i4++] = colorG*deact + colorWG*curPart.activation;
            pColor[i4++] = colorB*deact + colorWB*curPart.activation;
            pColor[i4++] = 1/(sMin1 + pS*pS * (1 + 9f*deact));
        }
        ii++;
        touchMode = 0;
        dXOff = 0; // Reset
    }





    private class Particle{
        public float X = 0, Y = 0, Z = 0, vX = 0, vY = 0, vZ = 0, activation = 0;
        public Particle(){
            X = (float) Math.random()*2-1;
            Y = (float) Math.random()*2-1;
            Z = (float) Math.random()*2-1;
        }
    }
}
