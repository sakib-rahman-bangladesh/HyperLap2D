/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.view;

import com.badlogic.gdx.utils.viewport.ScreenViewport;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.view.stage.Sandbox;

/**
 * Mediates scene communication between editor and current runtime
 *
 * @author azakhary
 */
public class SceneControlMediator {

	private final HyperLap2DFacade facade;
	private final ProjectManager projectManager;
	/**
	 * main holder of the scene
	 */
	public SceneLoader sceneLoader;


	/**
	 * current scene tools
	 */
	private SceneVO currentSceneVo;

	/**
	 * tools object of the root element of the scene
	 */
	private CompositeItemVO rootSceneVO;

	public SceneControlMediator(SceneLoader sceneLoader) {
		this.sceneLoader = sceneLoader;
		facade = HyperLap2DFacade.getInstance();
		projectManager = facade.retrieveProxy(ProjectManager.NAME);
	}

	public ProjectInfoVO getProjectInfoVO() {
		return sceneLoader.getRm().getProjectVO();
	}

	public void initScene(String sceneName) {
		ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
		ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);

		ScreenViewport viewport = new ScreenViewport();
		float sandboxUpp = Sandbox.getInstance().getUIStage().getUIScaleDensity();
		float upp = 1f / resourceManager.getProjectVO().pixelToWorld * sandboxUpp;
		viewport.setUnitsPerPixel(upp);

		currentSceneVo = sceneLoader.loadScene(sceneName, viewport);

		rootSceneVO = new CompositeItemVO(currentSceneVo.composite);
		Sandbox.getInstance().getEngine().process();
	}

	public void updateAmbientLights() {
		sceneLoader.setAmbientInfo(sceneLoader.getSceneVO());
	}

	public CompositeItemVO getRootSceneVO() {
		return rootSceneVO;
	}

	public SceneVO getCurrentSceneVO() {
		return currentSceneVo;
	}
	
	public int getRootEntity() {
		return sceneLoader.getRoot();
	}
}
