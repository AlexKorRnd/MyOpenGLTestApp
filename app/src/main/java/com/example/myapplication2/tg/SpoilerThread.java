package com.example.myapplication2.tg;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.util.Log;

import com.example.myapplication2.R;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * @author a.s.korchagin
 */
public class SpoilerThread extends Thread {

    public final int MAX_FPS;
    private final double MIN_DELTA;
    private final double MAX_DELTA;

    private volatile boolean running = true;
    private volatile boolean paused = false;

    private final Runnable invalidate;
    private final SurfaceTexture surfaceTexture;
    private final Object resizeLock = new Object();
    private boolean resize;
    private int width, height;
    private int particlesCount;
    private float radius = AndroidUtilities.dpf2(1.2F);

    public SpoilerThread(SurfaceTexture surfaceTexture, int width, int height, Runnable invalidate) {
        this.invalidate = invalidate;
        this.surfaceTexture = surfaceTexture;
        this.width = width;
        this.height = height;
        this.particlesCount = particlesCount();

        MAX_FPS = (int) AndroidUtilities.screenRefreshRate;
        MIN_DELTA = 1.0 / MAX_FPS;
        MAX_DELTA = MIN_DELTA * 4;
    }

    private int particlesCount() {
        return (int) Utilities.clamp(width * height / (500f * 500f) * 1000, 10000, 500);
    }

    public void updateSize(int width, int height) {
        synchronized (resizeLock) {
            resize = true;
            this.width = width;
            this.height = height;
        }
    }

    public void halt() {
        running = false;
    }

    public void pause(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void run() {
        init();
        long lastTime = System.nanoTime();
        while (running) {
            final long now = System.nanoTime();
            double Δt = (now - lastTime) / 1_000_000_000.;
            lastTime = now;
            Log.v("test____2", "run");

            if (Δt < MIN_DELTA) {
                double wait = MIN_DELTA - Δt;
                try {
                    long milli = (long) (wait * 1000L);
                    int nano = (int) ((wait - milli / 1000.) * 1_000_000_000);
                    sleep(milli, nano);
                } catch (Exception ignore) {
                }
                Δt = MIN_DELTA;
            } else if (Δt > MAX_DELTA) {
                Δt = MAX_DELTA;
            }

            while (paused) {
                try {
                    sleep(1000);
                } catch (Exception ignore) {
                }
            }

            checkResize();
            drawFrame((float) Δt);

            AndroidUtilities.cancelRunOnUIThread(this.invalidate);
            AndroidUtilities.runOnUIThread(this.invalidate);
        }
        die();
    }

    private EGL10 egl;
    private EGLDisplay eglDisplay;
    private EGLConfig eglConfig;
    private EGLSurface eglSurface;
    private EGLContext eglContext;

    private int drawProgram;
    private int resetHandle;
    private int timeHandle;
    private int deltaTimeHandle;
    private int sizeHandle;
    private int radiusHandle;
    private int seedHandle;

    private boolean reset = true;

    private int currentBuffer = 0;
    private int[] particlesData;

    private void init() {
        egl = (EGL10) javax.microedition.khronos.egl.EGLContext.getEGL();

        eglDisplay = egl.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == egl.EGL_NO_DISPLAY) {
            running = false;
            return;
        }
        int[] version = new int[2];
        if (!egl.eglInitialize(eglDisplay, version)) {
            running = false;
            return;
        }

        int[] configAttributes = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                EGL14.EGL_NONE
        };
        EGLConfig[] eglConfigs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!egl.eglChooseConfig(eglDisplay, configAttributes, eglConfigs, 1, numConfigs)) {
            running = false;
            return;
        }
        eglConfig = eglConfigs[0];

        int[] contextAttributes = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };
        eglContext = egl.eglCreateContext(eglDisplay, eglConfig, egl.EGL_NO_CONTEXT, contextAttributes);
        if (eglContext == null) {
            running = false;
            return;
        }

        eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, null);
        if (eglSurface == null) {
            running = false;
            return;
        }

        if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            running = false;
            return;
        }

        genParticlesData();

        // draw program (vertex and fragment shaders)
        int vertexShader = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
        int fragmentShader = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);
        if (vertexShader == 0 || fragmentShader == 0) {
            running = false;
            return;
        }
        String spoilerVertexName = AndroidUtilities.readRes(R.raw.spoiler_vertex) + "\n// " + Math.random();
        GLES31.glShaderSource(vertexShader, spoilerVertexName);
        GLES31.glCompileShader(vertexShader);
        int[] status = new int[1];
        GLES31.glGetShaderiv(vertexShader, GLES31.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            FileLog.e("SpoilerEffect2, compile vertex shader error: " + GLES31.glGetShaderInfoLog(vertexShader));
            GLES31.glDeleteShader(vertexShader);
            running = false;
            return;
        }
        GLES31.glShaderSource(fragmentShader, AndroidUtilities.readRes(R.raw.spoiler_fragment) + "\n// " + Math.random());
        GLES31.glCompileShader(fragmentShader);
        GLES31.glGetShaderiv(fragmentShader, GLES31.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            FileLog.e("SpoilerEffect2, compile fragment shader error: " + GLES31.glGetShaderInfoLog(fragmentShader));
            GLES31.glDeleteShader(fragmentShader);
            running = false;
            return;
        }
        drawProgram = GLES31.glCreateProgram();
        if (drawProgram == 0) {
            running = false;
            return;
        }
        GLES31.glAttachShader(drawProgram, vertexShader);
        GLES31.glAttachShader(drawProgram, fragmentShader);
        String[] feedbackVaryings = {"outPosition", "outVelocity", "outTime", "outDuration"};
        GLES31.glTransformFeedbackVaryings(drawProgram, feedbackVaryings, GLES31.GL_INTERLEAVED_ATTRIBS);

        GLES31.glLinkProgram(drawProgram);
        GLES31.glGetProgramiv(drawProgram, GLES31.GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            FileLog.e("SpoilerEffect2, link draw program error: " + GLES31.glGetProgramInfoLog(drawProgram));
            running = false;
            return;
        }

        resetHandle = GLES31.glGetUniformLocation(drawProgram, "reset");
        timeHandle = GLES31.glGetUniformLocation(drawProgram, "time");
        deltaTimeHandle = GLES31.glGetUniformLocation(drawProgram, "deltaTime");
        sizeHandle = GLES31.glGetUniformLocation(drawProgram, "size");
        radiusHandle = GLES31.glGetUniformLocation(drawProgram, "r");
        seedHandle = GLES31.glGetUniformLocation(drawProgram, "seed");

        GLES31.glViewport(0, 0, width, height);
        GLES31.glEnable(GLES31.GL_BLEND);
        GLES31.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES31.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        GLES31.glUseProgram(drawProgram);
        GLES31.glUniform2f(sizeHandle, width, height);
        GLES31.glUniform1f(resetHandle, reset ? 1 : 0);
        GLES31.glUniform1f(radiusHandle, radius);
        GLES31.glUniform1f(seedHandle, Utilities.fastRandom.nextInt(256) / 256f);
    }

    private float t;
    private final float timeScale = .65f;

    private void drawFrame(float Δt) {
        if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            running = false;
            return;
        }

        t += Δt * timeScale;
        if (t > 1000.f) {
            t = 0;
        }

        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, particlesData[currentBuffer]);
        GLES31.glVertexAttribPointer(0, 2, GLES31.GL_FLOAT, false, 24, 0); // Position (vec2)
        GLES31.glEnableVertexAttribArray(0);
        GLES31.glVertexAttribPointer(1, 2, GLES31.GL_FLOAT, false, 24, 8); // Velocity (vec2)
        GLES31.glEnableVertexAttribArray(1);
        GLES31.glVertexAttribPointer(2, 1, GLES31.GL_FLOAT, false, 24, 16); // Time (float)
        GLES31.glEnableVertexAttribArray(2);
        GLES31.glVertexAttribPointer(3, 1, GLES31.GL_FLOAT, false, 24, 20); // Duration (float)
        GLES31.glEnableVertexAttribArray(3);
        GLES31.glBindBufferBase(GLES31.GL_TRANSFORM_FEEDBACK_BUFFER, 0, particlesData[1 - currentBuffer]);
        GLES31.glVertexAttribPointer(0, 2, GLES31.GL_FLOAT, false, 24, 0); // Position (vec2)
        GLES31.glEnableVertexAttribArray(0);
        GLES31.glVertexAttribPointer(1, 2, GLES31.GL_FLOAT, false, 24, 8); // Velocity (vec2)
        GLES31.glEnableVertexAttribArray(1);
        GLES31.glVertexAttribPointer(2, 1, GLES31.GL_FLOAT, false, 24, 16); // Time (float)
        GLES31.glEnableVertexAttribArray(2);
        GLES31.glVertexAttribPointer(3, 1, GLES31.GL_FLOAT, false, 24, 20); // Duration (float)
        GLES31.glEnableVertexAttribArray(3);
        GLES31.glUniform1f(timeHandle, t);
        GLES31.glUniform1f(deltaTimeHandle, Δt * timeScale);
        GLES31.glBeginTransformFeedback(GLES31.GL_POINTS);
        GLES31.glDrawArrays(GLES31.GL_POINTS, 0, particlesCount);
        GLES31.glEndTransformFeedback();

        if (reset) {
            reset = false;
            GLES31.glUniform1f(resetHandle, 0f);
        }
        currentBuffer = 1 - currentBuffer;

        egl.eglSwapBuffers(eglDisplay, eglSurface);

        checkGlErrors();
    }

    private void die() {
        if (particlesData != null) {
            try {
                GLES31.glDeleteBuffers(2, particlesData, 0);
            } catch (Exception e) {
                FileLog.e(e);
            }
            ;
            particlesData = null;
        }
        if (drawProgram != 0) {
            try {
                GLES31.glDeleteProgram(drawProgram);
            } catch (Exception e) {
                FileLog.e(e);
            }
            ;
            drawProgram = 0;
        }
        if (egl != null) {
            try {
                egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            } catch (Exception e) {
                FileLog.e(e);
            }
            ;
            try {
                egl.eglDestroySurface(eglDisplay, eglSurface);
            } catch (Exception e) {
                FileLog.e(e);
            }
            ;
            try {
                egl.eglDestroyContext(eglDisplay, eglContext);
            } catch (Exception e) {
                FileLog.e(e);
            }
            ;
        }
        try {
            surfaceTexture.release();
        } catch (Exception e) {
            FileLog.e(e);
        }
        ;

        checkGlErrors();
    }

    private void checkResize() {
        synchronized (resizeLock) {
            if (resize) {
                GLES31.glUniform2f(sizeHandle, width, height);
                GLES31.glViewport(0, 0, width, height);
                int newParticlesCount = particlesCount();
                if (newParticlesCount > this.particlesCount) {
                    reset = true;
                    genParticlesData();
                }
                this.particlesCount = newParticlesCount;
                resize = false;
            }
        }
    }

    private void genParticlesData() {
        if (particlesData != null) {
            GLES31.glDeleteBuffers(2, particlesData, 0);
        }

        particlesData = new int[2];
        GLES31.glGenBuffers(2, particlesData, 0);

        for (int i = 0; i < 2; ++i) {
            GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, particlesData[i]);
            GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, this.particlesCount * 6 * 4, null, GLES31.GL_DYNAMIC_DRAW);
        }

        checkGlErrors();
    }

    private void checkGlErrors() {
        int err;
        while ((err = GLES31.glGetError()) != GLES31.GL_NO_ERROR) {
            FileLog.e("spoiler gles error " + err);
        }
    }
}
