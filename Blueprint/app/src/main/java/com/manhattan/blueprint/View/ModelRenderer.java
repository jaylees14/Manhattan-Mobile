package com.manhattan.blueprint.View;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.manhattan.blueprint.Model.DAO.Maybe;
import com.manhattan.blueprint.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.RajawaliRenderer;

public class ModelRenderer extends RajawaliRenderer {
    private Context context;
    private int modelID;
    private Object3D model;

    public ModelRenderer(Context context, int modelID) {
        super(context);
        this.context = context;
        this.modelID = modelID;
        setFrameRate(60);
    }

    public void initScene() {
        DirectionalLight directionalLight = new DirectionalLight(1f, .2f, -1.0f);
        directionalLight.setColor(1.0f, 1.0f, 1.0f);
        directionalLight.setPower(2);
        getCurrentScene().addLight(directionalLight);

        Maybe<Integer> modelImageID = getResourceID(context, "model" + modelID + "_obj");
        if (!modelImageID.isPresent()) {
            modelImageID = getResourceID(context, "modeldefault_obj");
        }

        try {
            LoaderOBJ loaderOBJ = new LoaderOBJ(context.getResources(), getTextureManager(), modelImageID.get());
            loaderOBJ.parse();
            model = loaderOBJ.getParsedObject();

            getCurrentScene().addChild(model);
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        getCurrentCamera().setZ(getCurrentCamera().getZ() + 1.0f);
        getCurrentScene().setBackgroundColor(context.getColor(R.color.brandPrimaryDark));
    }


    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        model.rotate(Vector3.Axis.Y, 0.5f);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    private static Maybe<Integer> getResourceID(Context context, String resName) {
        try {
            int identifier = context.getResources().getIdentifier(resName, "raw", context.getPackageName());
            return identifier == 0 ? Maybe.empty() : Maybe.of(identifier);
        } catch (Exception e) {
            return Maybe.empty();
        }
    }
}
