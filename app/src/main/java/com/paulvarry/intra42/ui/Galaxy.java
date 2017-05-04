package com.paulvarry.intra42.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import com.paulvarry.intra42.R;
import com.paulvarry.intra42.api.model.ProjectDataIntra;

import java.util.ArrayList;
import java.util.List;

public class Galaxy extends View {

    static int GRAPH_MAP_LIMIT_MIN = -2000;
    static int GRAPH_MAP_LIMIT_MAX = 2000;
    static int GRAPH_MAP_MIN = -3000;
    static int GRAPH_MAP_MAX = 2000;
    static int TEXT_HEIGHT = 25;
    ProjectDataIntra projectDataFirstInternship = null;
    ProjectDataIntra projectDataFinalInternship = null;
    private float weightPath;
    private int backgroundColor;
    private int colorProjectUnavailable;
    private int colorProjectAvailable;
    private int colorProjectValidated;
    private int colorProjectInProgress;
    private int colorProjectFailed;
    private int colorProjectTextUnavailable;
    private int colorProjectTextAvailable;
    private int colorProjectTextValidated;
    private int colorProjectTextInProgress;
    private int colorProjectTextFailed;
    private List<ProjectDataIntra> data;
    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private ValueAnimator mScrollAnimator;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1f;
    private Paint mPaintBackground;
    private Paint mPaintPath;
    private Paint mPaintProject;
    private Paint mPaintText;
    private float posY;
    private float posX;
    private int height;
    private int width;

    private OnProjectClickListener onClickListener;

    public Galaxy(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Galaxy,
                0, 0);

        try {
            int defaultTextColor = Color.parseColor("#3F51B5");
            backgroundColor = attributes.getColor(R.styleable.Galaxy_backgroundColor, 0);
            colorProjectUnavailable = attributes.getColor(R.styleable.Galaxy_colorProjectUnavailable, 0);
            colorProjectAvailable = attributes.getColor(R.styleable.Galaxy_colorProjectAvailable, 0);
            colorProjectValidated = attributes.getColor(R.styleable.Galaxy_colorProjectValidated, 0);
            colorProjectFailed = attributes.getColor(R.styleable.Galaxy_colorProjectFailed, 0);
            colorProjectInProgress = attributes.getColor(R.styleable.Galaxy_colorProjectInProgress, 0);
            colorProjectTextUnavailable = attributes.getColor(R.styleable.Galaxy_colorProjectTextUnavailable, defaultTextColor);
            colorProjectTextAvailable = attributes.getColor(R.styleable.Galaxy_colorProjectTextAvailable, defaultTextColor);
            colorProjectTextValidated = attributes.getColor(R.styleable.Galaxy_colorProjectTextValidated, defaultTextColor);
            colorProjectTextFailed = attributes.getColor(R.styleable.Galaxy_colorProjectTextFailed, defaultTextColor);
            colorProjectTextInProgress = attributes.getColor(R.styleable.Galaxy_colorProjectTextInProgress, defaultTextColor);
            weightPath = attributes.getDimension(R.styleable.Galaxy_weightPath, 1.f);

        } finally {
            attributes.recycle();
        }

        init();
    }

    private void init() {
        mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBackground.setColor(backgroundColor);
        mPaintBackground.setStyle(Paint.Style.FILL);

        mPaintPath = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintPath.setAntiAlias(true);
        mPaintPath.setColor(colorProjectUnavailable);
        mPaintPath.setStrokeWidth(weightPath);
        mPaintPath.setStyle(Paint.Style.STROKE);

        mPaintProject = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintProject.setAntiAlias(true);
        mPaintProject.setColor(colorProjectUnavailable);

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setAntiAlias(true);
        mPaintText.setFakeBoldText(true);
        mPaintText.setTextAlign(Paint.Align.CENTER);


        // Create a Scroller to handle the fling gesture.
        mScroller = new Scroller(getContext(), null, true);

        // The scroller doesn't have any built-in animation functions--it just supplies
        // values when we ask it to. So we have to have a way to call it every frame
        // until the fling ends. This code (ab)uses a ValueAnimator object to generate
        // a callback on every animation frame. We don't use the animated value at all.
        mScrollAnimator = ValueAnimator.ofFloat(0, 1);
        mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tickScrollAnimation();
            }
        });

        // Create a gesture detector to handle onTouch messages
        mGestureDetector = new GestureDetector(this.getContext(), new GestureListener());

        // Turn off long press--this control doesn't use it, and if long press is enabled,
        // you can't scroll for a bit, pause, then scroll some more (the pause is interpreted
        // as a long press, apparently)
        mGestureDetector.setIsLongpressEnabled(false);

        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    private void tickScrollAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            posX = mScroller.getCurrX();
            posY = mScroller.getCurrY();
        } else {
            mScrollAnimator.cancel();
            onScrollFinished();
        }
    }

    /**
     * Called when the user finishes a scroll action.
     */
    private void onScrollFinished() {
        decelerate();
    }

    /**
     * Disable hardware acceleration (releases memory)
     */
    public void decelerate() {
        setLayerToSW(this);
    }

    private void setLayerToSW(View v) {
        if (!v.isInEditMode()) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void setOnProjectClickListener(OnProjectClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setData(List<ProjectDataIntra> data) {
        this.data = data;

        for (ProjectDataIntra projectData : data) {

            if (projectData.kind == ProjectDataIntra.Kind.FIRST_INTERNSHIP)
                projectDataFirstInternship = projectData;
            else if (projectData.kind == ProjectDataIntra.Kind.SECOND_INTERNSHIP)
                projectDataFinalInternship = projectData;
        }

        if (projectDataFirstInternship != null) {
            projectDataFirstInternship.x = 3680;
            projectDataFirstInternship.y = 3750;
        }
        if (projectDataFinalInternship != null) {
            projectDataFinalInternship.x = 4600;
            projectDataFinalInternship.y = 4600;
        }

        onScrollFinished();
        invalidate();
    }

    /**
     * Enable hardware acceleration (consumes memory)
     */
    public void accelerate() {
        setLayerToHW(this);
    }

    private void setLayerToHW(View v) {
  /*      if (!v.isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Let the GestureDetector interpret this event
        boolean resultGesture = mGestureDetector.onTouchEvent(event);
        boolean resultScale = mScaleDetector.onTouchEvent(event);

        // If the GestureDetector doesn't want this event, do some custom processing.
        // This code just tries to detect when the user is done scrolling by looking
        // for ACTION_UP events.
        if (!resultGesture && !resultScale) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // User is done scrolling, it's now safe to do things like autocenter
                stopScrolling();
                resultGesture = true;
            }
        }

        return resultGesture;
    }

    /**
     * Force a stop to all pie motion. Called when the user taps during a fling.
     */
    private void stopScrolling() {
        mScroller.forceFinished(true);

        onScrollFinished();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        height = h;
        width = w;

        posX = 0;
        posY = 0;

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect c = canvas.getClipBounds();

        canvas.drawRect(c, mPaintBackground);

        if (data == null)
            return;

        mPaintProject.setStrokeWidth(weightPath * mScaleFactor);
        mPaintPath.setStrokeWidth(weightPath * mScaleFactor);
        mPaintText.setTextSize(TEXT_HEIGHT * mScaleFactor);

        // draw projects path
        for (ProjectDataIntra projectData : data) {
            if (projectData.by != null)
                for (ProjectDataIntra.By by : projectData.by) {

                    float startX = getDrawPosX(by.points.get(0).get(0));
                    float startY = getDrawPosY(by.points.get(0).get(1));
                    float stopX = getDrawPosX(by.points.get(1).get(0));
                    float stopY = getDrawPosY(by.points.get(1).get(1));

                    canvas.drawLine(startX, startY, stopX, stopY, getColorPath(projectData));
                }
        }

        canvas.drawCircle(
                getDrawPosX(3000),
                getDrawPosY(3000),
                1000 * mScaleFactor,
                getColorPath(projectDataFirstInternship));
        canvas.drawCircle(
                getDrawPosX(3000),
                getDrawPosY(3000),
                2250 * mScaleFactor,
                getColorPath(projectDataFinalInternship));

        // draw projects
        for (ProjectDataIntra projectData : data) {

            switch (projectData.kind) {
                case PROJECT:
                    drawProject(canvas, projectData, ProjectRadius.PROJECT);
                    break;
                case BIG_PROJECT:
                    drawProject(canvas, projectData, ProjectRadius.BIG_PROJECT);
                    break;
                case PART_TIME:
                    drawProject(canvas, projectData, ProjectRadius.PART_TIME);
                    break;
                case FIRST_INTERNSHIP:
                    drawProject(canvas, projectData, ProjectRadius.FIRST_INTERNSHIP);
                    break;
                case SECOND_INTERNSHIP:
                    drawProject(canvas, projectData, ProjectRadius.SECOND_INTERNSHIP);
                    break;
                case EXAM:
                    drawProject(canvas, projectData, ProjectRadius.EXAM);
                    break;
                case PISCINE:
                    drawPiscine(canvas, projectData);
                    break;
                case RUSH:
                    drawRush(canvas, projectData);
                    break;
            }
        }

        if (mScrollAnimator.isRunning()) {
            tickScrollAnimation();
            postInvalidate();
        }
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
        invalidate();
        requestLayout();
    }

    private Paint getPaintProject(ProjectDataIntra projectData) {
        mPaintProject.setColor(getColor(projectData));

        return mPaintProject;
    }

    private Paint getPaintProjectText(ProjectDataIntra projectData) {
        mPaintText.setColor(getColorText(projectData));

        return mPaintText;
    }

    private int getColor(ProjectDataIntra projectData) {

        if (projectData != null)
            switch (projectData.state) {
                case DONE:
                    return colorProjectValidated;

                case AVAILABLE:
                    return colorProjectAvailable;

                case IN_PROGRESS:
                    return colorProjectInProgress;

                case UNAVAILABLE:
                    return colorProjectUnavailable;

            }
        return colorProjectUnavailable;
    }

    private int getColorText(ProjectDataIntra projectData) {

        if (projectData != null)
            switch (projectData.state) {
                case DONE:
                    return colorProjectTextValidated;

                case AVAILABLE:
                    return colorProjectTextAvailable;

                case IN_PROGRESS:
                    return colorProjectTextInProgress;

                case UNAVAILABLE:
                    return colorProjectTextUnavailable;

            }
        return colorProjectTextUnavailable;
    }

    private void drawProject(Canvas canvas, ProjectDataIntra projectData, int size) {

        canvas.drawCircle(
                getDrawPosX(projectData),
                getDrawPosY(projectData),
                getDrawRadius(size),
                getPaintProject(projectData));

        drawProjectTitle(canvas, projectData);
    }

    float getProjectDrawWidth(ProjectDataIntra projectData) {
        switch (projectData.kind) {
            case PROJECT:
                return getProjectDrawWidthCircle(projectData);
            case BIG_PROJECT:
                return getProjectDrawWidthCircle(projectData);
            case PART_TIME:
                return getProjectDrawWidthCircle(projectData);
            case FIRST_INTERNSHIP:
                return getProjectDrawWidthCircle(projectData);
            case SECOND_INTERNSHIP:
                return getProjectDrawWidthCircle(projectData);
            case EXAM:
                return getProjectDrawWidthCircle(projectData);
            case PISCINE:
                return -1;
            case RUSH:
                return -1;
        }
        return -1;
    }

    float getProjectDrawWidthCircle(ProjectDataIntra projectData) {
        return getDrawRadius(projectData) * 2;
    }

    private void drawProjectTitle(Canvas canvas, ProjectDataIntra projectData) {
        Paint paintText = getPaintProjectText(projectData);

        float projectWidth = getProjectDrawWidth(projectData);
        float textWidth = paintText.measureText(projectData.name);

        List<String> textToDraw = new ArrayList<>();

        if (projectWidth != -1 && projectWidth < textWidth) {
            int numberCut = (int) (textWidth / projectWidth) + 1;
            String tmpText = projectData.name;
            int posToCut = tmpText.length() / numberCut;

            int i = 0;
            while (true) {
                posToCut = splitAt(tmpText, posToCut);
                if (posToCut == -1) {
                    textToDraw.add(tmpText);
                    break;
                }

                if (tmpText.charAt(posToCut) == ' ') {
                    textToDraw.add(tmpText.substring(0, posToCut));
                    tmpText = tmpText.substring(posToCut + 1);
                } else {
                    textToDraw.add(tmpText.substring(0, posToCut + 1));
                    tmpText = tmpText.substring(posToCut + 1);
                }
                posToCut = tmpText.length() / numberCut - i;
                i++;
            }

        } else
            textToDraw.add(projectData.name);

        float textHeight = paintText.getTextSize();
        float posYStartDraw = getDrawPosY(projectData) - (textHeight * (textToDraw.size() - 1)) / 2;
        for (int i = 0; i < textToDraw.size(); i++) {

            float heightTextDraw = posYStartDraw + textHeight * i - (paintText.descent() + paintText.ascent()) / 2;
            canvas.drawText(
                    textToDraw.get(i),
                    getDrawPosX(projectData),
                    heightTextDraw,
                    paintText);
        }

    }

    private int splitAt(String stringToSplit, int posSplit) {

        if (posSplit < 0 || stringToSplit == null || stringToSplit.length() <= posSplit)
            return -1;

        if (isSplitableChar(stringToSplit.charAt(posSplit)))
            return posSplit;

        int stringLength = stringToSplit.length();
        int searchShift = 0;

        boolean pursueBefore = true;
        boolean pursueAfter = true;
        while (pursueBefore || pursueAfter) {

            if (pursueBefore && posSplit - searchShift >= 0) {
                if (isSplitableChar(stringToSplit.charAt(posSplit - searchShift)))
                    return posSplit - searchShift;
            } else
                pursueBefore = false;
            if (pursueAfter && posSplit + searchShift < stringLength) {
                if (isSplitableChar(stringToSplit.charAt(posSplit + searchShift)))
                    return posSplit + searchShift;
            } else
                pursueAfter = false;

            searchShift++;
        }

        return -1;
    }

    boolean isSplitableChar(char c) {
        return c == ' ' || c == '-' || c == '_';

    }

    private void drawPiscine(Canvas canvas, ProjectDataIntra projectData) {

        float x = getDrawPosX(projectData);
        float y = getDrawPosY(projectData);

        float width = ProjectRectSize.PISCINE_WIDTH * mScaleFactor;
        float height = ProjectRectSize.PISCINE_HEIGHT * mScaleFactor;

        float left = x - width / 2;
        float top = y + height / 2;
        float right = x + width / 2;
        float bottom = y - height / 2;

        canvas.drawRect(left, top, right, bottom, getPaintProject(projectData));
        drawProjectTitle(canvas, projectData);
    }

    private void drawRush(Canvas canvas, ProjectDataIntra projectData) {

        float x = getDrawPosX(projectData.x);
        float y = getDrawPosY(projectData.y);

        float width = ProjectRectSize.RUSH_WIDTH * mScaleFactor;
        float height = ProjectRectSize.RUSH_HEIGHT * mScaleFactor;

        float left = x - width / 2;
        float top = y + height / 2;
        float right = x + width / 2;
        float bottom = y - height / 2;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            canvas.drawRoundRect(left, top, right, bottom, 100, 100, getPaintProject(projectData));
        else
            canvas.drawRect(left, top, right, bottom, getPaintProject(projectData));

        drawProjectTitle(canvas, projectData);
    }

    private Paint getColorPath(ProjectDataIntra projectData) {
        mPaintPath.setColor(getColor(projectData));
        return mPaintPath;
    }

    private boolean isAnimationRunning() {
        return !mScroller.isFinished();
    }

    boolean ptInsideCircle(float x, float y, ProjectDataIntra projectData) {

        float projectCenterX = getDrawPosX(projectData);
        float projectCenterY = getDrawPosY(projectData);

        double distanceBetween = Math.pow(x - projectCenterX, 2) + Math.pow(y - projectCenterY, 2);
        double radius = Math.pow(getDrawRadius(projectData), 2);

        return distanceBetween <= radius;
    }

    private float getDrawPosX(ProjectDataIntra projectData) {
        return getDrawPosX(projectData.x);
    }

    private float getDrawPosY(ProjectDataIntra projectData) {
        return getDrawPosY(projectData.y);
    }

    private float getDrawRadius(ProjectDataIntra projectData) {
        switch (projectData.kind) {
            case PROJECT:
                return getDrawRadius(ProjectRadius.PROJECT);
            case BIG_PROJECT:
                return getDrawRadius(ProjectRadius.BIG_PROJECT);
            case PART_TIME:
                return getDrawRadius(ProjectRadius.PART_TIME);
            case FIRST_INTERNSHIP:
                return getDrawRadius(ProjectRadius.FIRST_INTERNSHIP);
            case SECOND_INTERNSHIP:
                return getDrawRadius(ProjectRadius.SECOND_INTERNSHIP);
            case EXAM:
                return getDrawRadius(ProjectRadius.EXAM);
        }
        return 0;
    }

    private float getDrawPosX(float pos) {
        return ((pos - 3000) + posX) * mScaleFactor + width / 2;
    }

    private float getDrawPosY(float pos) {
        return ((pos - 3000) + posY) * mScaleFactor + height / 2;
    }

    private float getDrawRadius(int size) {
        return size * mScaleFactor;
    }

    boolean ptInsideRectPiscine(float clickX, float clickY, ProjectDataIntra projectData) {

        float x = getDrawPosX(projectData);
        float y = getDrawPosY(projectData);

        float width = ProjectRectSize.PISCINE_WIDTH * mScaleFactor;
        float height = ProjectRectSize.PISCINE_HEIGHT * mScaleFactor;

        return clickX >= x &&
                clickX <= x + width &&
                clickY >= y &&
                clickY <= y + height;
    }

    boolean ptInsideRectRush(float clickX, float clickY, ProjectDataIntra projectData) {

        float x = getDrawPosX(projectData);
        float y = getDrawPosY(projectData);

        float width = ProjectRectSize.PISCINE_WIDTH * mScaleFactor;
        float height = ProjectRectSize.PISCINE_HEIGHT * mScaleFactor;

        return clickX >= x &&
                clickX <= x + width &&
                clickY >= y &&
                clickY <= y + height;
    }

    public interface OnProjectClickListener {
        void onClick(ProjectDataIntra projectData);
    }

    /**
     * Extends {@link GestureDetector.SimpleOnGestureListener} to provide custom gesture
     * processing.
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            float tempPosX = posX - distanceX * (1 / mScaleFactor);
            float tempPosY = posY - distanceY * (1 / mScaleFactor);

            if (tempPosX < GRAPH_MAP_LIMIT_MIN)
                tempPosX = GRAPH_MAP_LIMIT_MIN;
            else if (tempPosX > GRAPH_MAP_LIMIT_MAX)
                tempPosX = GRAPH_MAP_LIMIT_MAX;

            if (tempPosY < GRAPH_MAP_LIMIT_MIN)
                tempPosY = GRAPH_MAP_LIMIT_MIN;
            else if (tempPosY > GRAPH_MAP_LIMIT_MAX)
                tempPosY = GRAPH_MAP_LIMIT_MAX;

            posX = tempPosX;
            posY = tempPosY;

            postInvalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mScroller.fling(
                    (int) posX,
                    (int) posY,
                    (int) velocityX,
                    (int) velocityY,
                    GRAPH_MAP_LIMIT_MIN,
                    GRAPH_MAP_LIMIT_MAX,
                    GRAPH_MAP_LIMIT_MIN,
                    GRAPH_MAP_LIMIT_MAX);

            // Start the animator and tell it to animate for the expected duration of the fling.
            mScrollAnimator.setDuration(mScroller.getDuration());
            mScrollAnimator.start();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // The user is interacting with the pie, so we want to turn on acceleration
            // so that the interaction is smooth.
            accelerate();
            if (isAnimationRunning()) {
                stopScrolling();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mScaleFactor *= 1.5f;
            postInvalidate();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            float x = e.getX();
            float y = e.getY();

            boolean clicked = false;

            for (ProjectDataIntra p : data) {
                if (p.kind == ProjectDataIntra.Kind.PISCINE) {
                    if (ptInsideRectPiscine(x, y, p)) {
                        clicked = true;
                        if (onClickListener != null)
                            onClickListener.onClick(p);
                        break;
                    }
                } else if (p.kind == ProjectDataIntra.Kind.RUSH) {
                    if (ptInsideRectRush(x, y, p)) {
                        clicked = true;
                        if (onClickListener != null)
                            onClickListener.onClick(p);
                        break;
                    }
                } else if (ptInsideCircle(x, y, p)) {
                    clicked = true;
                    if (onClickListener != null)
                        onClickListener.onClick(p);
                    break;
                }
            }
            return clicked;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.001f, Math.min(mScaleFactor, 5.0f));

            invalidate();
            return true;
        }
    }

    private class ProjectRadius {
        private static final int PROJECT = 60;
        private static final int BIG_PROJECT = 75;
        private static final int PART_TIME = 150;
        private static final int FIRST_INTERNSHIP = 100;
        private static final int SECOND_INTERNSHIP = 100;
        private static final int EXAM = 75;
    }

    private class ProjectRectSize {
        private static final int PISCINE_HEIGHT = 60;
        private static final int PISCINE_WIDTH = 250;
        private static final int RUSH_HEIGHT = 60;
        private static final int RUSH_WIDTH = 180;
    }
}