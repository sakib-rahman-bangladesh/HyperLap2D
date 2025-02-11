package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.resource.DeleteImageResource;
import games.rednblack.editor.controller.commands.resource.DeleteSpineAnimation;
import games.rednblack.editor.controller.commands.resource.DeleteSpriteAnimation;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.TexturePackVO;
import games.rednblack.editor.view.menu.ResourcesMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

public class ImagesPackDialogMediator extends Mediator<AtlasesPackDialog> {
    private static final String TAG = ImagesPackDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private static final String NEW_IMAGES_PACK = "games.rednblack.editor.view.ui.dialog.ImagesPackDialogMediator.NEW_IMAGES_PACK";
    private static final String MOVE_REGION_TO_PACK = "games.rednblack.editor.view.ui.dialog.ImagesPackDialogMediator.MOVE_REGION_TO_PACK";
    private static final String UPDATE_CURRENT_LIST = "games.rednblack.editor.view.ui.dialog.ImagesPackDialogMediator.UPDATE_CURRENT_LIST";
    private static final String REMOVE_PACK = "games.rednblack.editor.view.ui.dialog.ImagesPackDialogMediator.REMOVE_PACK";

    public ImagesPackDialogMediator() {
        super(NAME, new AtlasesPackDialog("Images Atlases", NEW_IMAGES_PACK, MOVE_REGION_TO_PACK, UPDATE_CURRENT_LIST, REMOVE_PACK));
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                ResourcesMenu.OPEN_IMAGES_PACK,
                ProjectManager.PROJECT_OPENED,
                NEW_IMAGES_PACK,
                MOVE_REGION_TO_PACK,
                UPDATE_CURRENT_LIST,
                REMOVE_PACK,
                MsgAPI.UPDATE_ATLAS_PACK_LIST,
                ProjectManager.PROJECT_DATA_UPDATED,
                DeleteImageResource.DONE,
                DeleteSpineAnimation.DONE,
                DeleteSpriteAnimation.DONE
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        String currentTab;
        switch (notification.getName()) {
            case ResourcesMenu.OPEN_IMAGES_PACK:
                viewComponent.show(uiStage);
                break;
            case ProjectManager.PROJECT_DATA_UPDATED:
            case DeleteImageResource.DONE:
            case DeleteSpineAnimation.DONE:
            case DeleteSpriteAnimation.DONE:
                viewComponent.updateMainPack(projectManager.currentProjectInfoVO.imagesPacks.get("main").regions);
                currentTab = viewComponent.getSelectedTab();
                if (currentTab != null)
                    viewComponent.updateCurrentPack(projectManager.currentProjectInfoVO.imagesPacks.get(currentTab).regions);
                else
                    viewComponent.clearCurrentPack();
                break;
            case MsgAPI.UPDATE_ATLAS_PACK_LIST:
            case ProjectManager.PROJECT_OPENED:
                viewComponent.initPacks(projectManager.currentProjectInfoVO.imagesPacks.keySet());
                viewComponent.updateMainPack(projectManager.currentProjectInfoVO.imagesPacks.get("main").regions);
                if (viewComponent.getSelectedTab() == null)
                    viewComponent.clearCurrentPack();
                break;
            case NEW_IMAGES_PACK:
                TexturePackVO newVo = new TexturePackVO();
                newVo.name = notification.getBody();

                projectManager.currentProjectInfoVO.imagesPacks.put(newVo.name, newVo);

                viewComponent.addNewPack(newVo.name);
                projectManager.saveCurrentProject();
                break;
            case UPDATE_CURRENT_LIST:
                currentTab = viewComponent.getSelectedTab();
                if (currentTab != null)
                    viewComponent.updateCurrentPack(projectManager.currentProjectInfoVO.imagesPacks.get(currentTab).regions);
                break;
            case MOVE_REGION_TO_PACK:
                String toPack = viewComponent.getMainSelected().size > 0 ? viewComponent.getSelectedTab() : "main";
                String fromPack = viewComponent.getMainSelected().size == 0 ? viewComponent.getSelectedTab() : "main";
                Array<String> regions = viewComponent.getMainSelected().size > 0 ? viewComponent.getMainSelected() : viewComponent.getCurrentSelected();

                for (String region : regions) {
                    projectManager.currentProjectInfoVO.imagesPacks.get(fromPack).regions.remove(region);
                    projectManager.currentProjectInfoVO.imagesPacks.get(toPack).regions.add(region);
                }

                viewComponent.updateCurrentPack(projectManager.currentProjectInfoVO.imagesPacks.get(viewComponent.getSelectedTab()).regions);
                viewComponent.updateMainPack(projectManager.currentProjectInfoVO.imagesPacks.get("main").regions);
                projectManager.saveCurrentProject();
                break;
            case REMOVE_PACK:
                String packToRemove = notification.getBody();
                projectManager.currentProjectInfoVO.imagesPacks.get("main").regions.addAll(
                        projectManager.currentProjectInfoVO.imagesPacks.get(packToRemove).regions
                );
                projectManager.currentProjectInfoVO.imagesPacks.remove(packToRemove);
                viewComponent.updateMainPack(projectManager.currentProjectInfoVO.imagesPacks.get("main").regions);
                viewComponent.clearCurrentPack();
                projectManager.saveCurrentProject();
                break;
        }
    }
}
