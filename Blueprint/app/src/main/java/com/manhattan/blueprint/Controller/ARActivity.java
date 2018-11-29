package com.manhattan.blueprint.Controller;

import com.google.gson.GsonBuilder;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.PermissionManager;
import com.manhattan.blueprint.Model.Resource;
import com.manhattan.blueprint.R;

public class ARActivity extends AppCompatActivity {
    private ArFragment arFragment;

    private boolean userRequestedARInstall = false;
    private ModelRenderable manhattanRenderable;
    private ViewRenderable testViewRenderable;
    private Resource resourceToCollect;

    private final int tapsRequired = 5;
    private int       collectCounter;
    private boolean   itemWasPlaced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        String jsonResource = (String) getIntent().getExtras().get("resource");
        resourceToCollect = (new GsonBuilder().create()).fromJson( jsonResource , Resource.class );

        PermissionManager cameraPermissionManager = new PermissionManager(0, Manifest.permission.CAMERA);
        if (!cameraPermissionManager.hasPermission(this)) {
            createDialog("Camera required",
                    "Please grant access to your camera so Blueprint can show resources around you.",
                    (dialog, which) -> finish());
        }
        itemWasPlaced = false;
        collectCounter = tapsRequired - 1;
    }

    public void onUpdate(FrameTime frameTime) {
        arFragment.onUpdate(frameTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arFragment != null) return;
        // Ensure latest version of ARCore is installed
        try {
            switch (ArCoreApk.getInstance().requestInstall(this, !userRequestedARInstall)) {
                case INSTALLED:
                    // Build renderable object
                    ViewRenderable.builder()
                            .setView(this, R.layout.resource_ar)
                            .build()
                            .thenAccept(renderable -> {
                                testViewRenderable = renderable;
                                TextView resourceView = renderable.getView().findViewById(R.id.Resource_AR);
                                resourceView.setText(resourceToCollect.getId());
                            });

                    // Start AR:
                    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById((R.id.ux_fragment));
                    arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);

                    // TODO: Uncomment to remove icon of a hand with device
                    // arFragment.getPlaneDiscoveryController().hide();
                    // arFragment.getPlaneDiscoveryController().setInstructionView(null);

                    arFragment.setOnTapArPlaneListener(
                            (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                                if (testViewRenderable == null) {
                                    return;
                                } else if(!itemWasPlaced) {
                                    // Create the Anchor.
                                    Anchor anchor = hitResult.createAnchor();
                                    AnchorNode anchorNode = new AnchorNode(anchor);
                                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                                    // Create the transformable node and add it to the anchor.
                                    TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
                                    node.setParent(anchorNode);
                                    node.setRenderable(testViewRenderable);
                                    node.select();

                                    node.setOnTapListener((hitTestResult, motionEvent1) -> {
                                        if(collectCounter > 0) {
                                            int progress = ((tapsRequired - collectCounter) * 100) / tapsRequired;
                                            Toast.makeText(this, "Progress: " + progress + "%",
                                                                              Toast.LENGTH_SHORT).show();
                                            collectCounter--;
                                        } else if(collectCounter == 0) {
                                            Toast.makeText(this, "You collected " + tapsRequired + " " +
                                                                               resourceToCollect.getId() + ". Well done!",
                                                                               Toast.LENGTH_LONG).show();
                                            InventoryItem itemCollected = new InventoryItem(resourceToCollect.getId(),
                                                                                            tapsRequired);
                                            BlueprintAPI api = new BlueprintAPI();
                                            api.makeRequest(api.inventoryService.addToInventory(itemCollected), new APICallback<Void>() {
                                                @Override
                                                public void success(Void response) {
                                                    finish();
                                                }

                                                @Override
                                                public void failure(int code, String error) {
                                                    AlertDialog.Builder failedCollectionDlg = new AlertDialog.Builder(ARActivity.this);
                                                    failedCollectionDlg.setTitle("Item collection failed!");
                                                    failedCollectionDlg.setMessage(error);
                                                    failedCollectionDlg.setCancelable(true);
                                                    failedCollectionDlg.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
                                                    failedCollectionDlg.create().show();
                                                }
                                            });

                                        }
                                    });
                                    node.getTranslationController().setEnabled(false);

                                    // Remove plane renderer
                                    arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
                                    itemWasPlaced = true;
                                }
                            });

                case INSTALL_REQUESTED:
                    // Ensures next call of request install returns INSTALLED or throws
                    userRequestedARInstall = true;
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            createDialog("ARCore required!",
                    "We require ARCore to show you resources. Please install and try again",
                    (dialog, which) -> finish());
        } catch (Exception e) {
            createDialog("Whoops!",
                    "Something went wrong: " + e.toString(),
                    (dialog, which) -> finish());
        }
    }

    private void createDialog(String title, String message, DialogInterface.OnClickListener onClick){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ARActivity.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Ok", (dialog, which) -> {
            dialog.dismiss();
            onClick.onClick(dialog, which);
        });
        alertDialog.create().show();
    }
}