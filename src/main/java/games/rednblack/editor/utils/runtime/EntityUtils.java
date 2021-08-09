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

package games.rednblack.editor.utils.runtime;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.*;
import games.rednblack.editor.renderer.components.light.LightBodyComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;

import java.util.*;
import java.util.function.Consumer;

import static games.rednblack.editor.renderer.factory.EntityFactory.*;

/**
 * Created by azakhary on 6/9/2015.
 */
public class EntityUtils {

    public static final HashMap<Integer, String> itemTypeIconMap = new HashMap<>();
    public static final HashMap<Integer, String> itemTypeNameMap = new HashMap<>();
    static {
        itemTypeNameMap.put(UNKNOWN_TYPE, "Unknown");
        itemTypeNameMap.put(COMPOSITE_TYPE, "Composite Item");
        itemTypeNameMap.put(PARTICLE_TYPE, "Particle Effect");
        itemTypeNameMap.put(LABEL_TYPE, "Label");
        itemTypeNameMap.put(IMAGE_TYPE, "Image");
        itemTypeNameMap.put(NINE_PATCH, "9-Patch Image");
        itemTypeNameMap.put(LIGHT_TYPE, "Light");
        itemTypeNameMap.put(SPINE_TYPE, "Spine Animation");
        itemTypeNameMap.put(SPRITE_TYPE, "Sprite Animation");
        itemTypeNameMap.put(COLOR_PRIMITIVE, "Primitive");
        itemTypeNameMap.put(TALOS_TYPE, "Talos VFX");

        itemTypeIconMap.put(UNKNOWN_TYPE, "icon-unknown");
        itemTypeIconMap.put(COMPOSITE_TYPE, "icon-root");
        itemTypeIconMap.put(PARTICLE_TYPE, "icon-particle-white");
        itemTypeIconMap.put(LABEL_TYPE, "icon-label");
        itemTypeIconMap.put(IMAGE_TYPE, "icon-image");
        itemTypeIconMap.put(NINE_PATCH, "icon-image");
        itemTypeIconMap.put(LIGHT_TYPE, "icon-particle-white");
        itemTypeIconMap.put(SPINE_TYPE, "icon-spine");
        itemTypeIconMap.put(SPRITE_TYPE, "icon-animation");
        itemTypeIconMap.put(COLOR_PRIMITIVE, "icon-image");
        itemTypeIconMap.put(TALOS_TYPE, "icon-particle-white");
    }

    public static String getItemName(int entity) {
        ParentNodeComponent parentNodeComponent = SandboxComponentRetriever.get(entity, ParentNodeComponent.class);
        if (parentNodeComponent == null)
            return Sandbox.getInstance().sceneControl.getCurrentSceneVO().sceneName;

        MainItemComponent mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
        if (mainItemComponent.itemIdentifier != null && !mainItemComponent.itemIdentifier.isEmpty()) {
            return mainItemComponent.itemIdentifier;
        } else {
            int type = EntityUtils.getType(entity);
            String name = itemTypeNameMap.get(type);
            if (name != null)
                return name;
            else
                return itemTypeNameMap.get(UNKNOWN_TYPE);
        }
    }

    public static Drawable getItemIcon(int entity) {
        int type = EntityUtils.getType(entity);
        String icon = itemTypeIconMap.get(type);
        return VisUI.getSkin().getDrawable(icon);
    }

    public static Integer getEntityId(int entity) {
        MainItemComponent mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
        return mainItemComponent.uniqueId;
    }

    public static Array<Integer> getEntityId(Iterable<Integer> entities) {
        Array<Integer> entityIds = new Array<>();
        for (int entity : entities) {
            MainItemComponent mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
            if (mainItemComponent != null)
                entityIds.add(mainItemComponent.uniqueId);
        }

        return entityIds;
    }

    public static int getByUniqueId(Integer id) {
        return Sandbox.getInstance().getSceneControl().sceneLoader.getEntityFactory().getEntityByUniqueId(id);
    }

    public static HashSet<Integer> getByUniqueId(Array<Integer> ids) {
        HashSet<Integer> entities = new HashSet<>();
        for (Integer id : ids) {
            Integer entity = Sandbox.getInstance().getSceneControl().sceneLoader.getEntityFactory().getEntityByUniqueId(id);
            entities.add(entity);
        }
        return entities;
    }

    /*public static HashMap<Integer, Collection<Component>> cloneEntities(Set<Integer> entities) {
        HashMap<Integer, Collection<Component>> data = new HashMap<>();

        for (int entity : entities) {
            Collection<Component> components = cloneEntityComponents(entity);
            data.put(EntityUtils.getEntityId(entity), components);
        }

        return data;
    }

    public static int cloneEntity(com.artemis.World engine, int entity) {
        int newEntity = engine.create();
        Collection<Component> components = cloneEntityComponents(entity);
        for (Component component : components) {
            Component c = engine.edit(newEntity).create(component.getClass());
            ComponentCloner.set(c, component);
        }
        return newEntity;
    }

    public static Collection<Component> cloneEntityComponents(int entity) {
        Array<Component> array = new Array<>();
        Collection<Component> components = ComponentCloner.cloneAll(ComponentRetriever.getComponents(entity, array));
        return components;
    }*/

    public static Vector2 getPosition(int entity) {
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        return new Vector2(transformComponent.x, transformComponent.y);
    }

    public static void getPosition(int entity, Vector2 position) {
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        position.set(transformComponent.x, transformComponent.y);
    }

    public static TransformComponent setPosition(int entity, Vector2 position) {
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        transformComponent.x = position.x;
        transformComponent.y = position.y;
        return transformComponent;
    }

    public static Vector2 getSize(int entity) {
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        return new Vector2(dimensionsComponent.width, dimensionsComponent.height);
    }

    public static void getSize(int entity, Vector2 size) {
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        size.set(dimensionsComponent.width, dimensionsComponent.height);
    }

    public static DimensionsComponent setSize(int entity, Vector2 size) {
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        dimensionsComponent.width = size.x;
        dimensionsComponent.height = size.y;
        if (dimensionsComponent.boundBox != null) {
            dimensionsComponent.boundBox.width = size.x;
            dimensionsComponent.boundBox.height = size.y;
        }
        return dimensionsComponent;
    }

    public static Vector2 getRightTopPoint(Set<Integer> entities) {
        if (entities.size() == 0) return null;

        Vector2 rightTopPoint = getPosition(entities.stream().findFirst().get());

        for (int entity : entities) {
            TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
            DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

            if (rightTopPoint.x < transformComponent.x + dimensionsComponent.width) {
                rightTopPoint.x = transformComponent.x + dimensionsComponent.width;
            }
            if (rightTopPoint.y < transformComponent.y + dimensionsComponent.height) {
                rightTopPoint.y = transformComponent.y + dimensionsComponent.height;
            }
        }

        return rightTopPoint;
    }

    public static Vector2 getLeftBottomPoint(Set<Integer> entities) {
        if (entities.size() == 0) return null;

        Vector2 leftBottomPoint = getPosition(entities.stream().findFirst().get());

        for (int entity : entities) {
            TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
            if (leftBottomPoint.x > transformComponent.x) {
                leftBottomPoint.x = transformComponent.x;
            }
            if (leftBottomPoint.y > transformComponent.y) {
                leftBottomPoint.y = transformComponent.y;
            }
        }

        return leftBottomPoint;
    }

    public static void changeParent(HashSet<Integer> entities, int parent) {
        for (int entity : entities) {
            ParentNodeComponent parentNodeComponent = SandboxComponentRetriever.get(entity, ParentNodeComponent.class);

            //remove me from previous parent children list
            NodeComponent nodeComponent = SandboxComponentRetriever.get(parentNodeComponent.parentEntity, NodeComponent.class);
            nodeComponent.children.removeValue(entity, true);

            //add me to new parent child list
            NodeComponent rootNodeComponent = SandboxComponentRetriever.get(parent, NodeComponent.class);
            rootNodeComponent.children.add(entity);

            //change my parent
            parentNodeComponent.parentEntity = parent;
        }
    }

    public static HashSet<Integer> getChildren(int entity) {
        HashSet<Integer> entities;
        NodeComponent nodeComponent = SandboxComponentRetriever.get(entity, NodeComponent.class);
        if (nodeComponent == null)
            return null;
        Integer[] children = nodeComponent.children.toArray();
        entities = new HashSet<>(Arrays.asList(children));

        return entities;
    }

    public static int getType(int entity) {
        MainItemComponent mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
        if (mainItemComponent == null)
            return UNKNOWN_TYPE;
        return mainItemComponent.entityType;
    }

    public static Array<Integer> getByLibraryLink(String link) {
        Array<Integer> result = new Array<>();
        EntitySubscription subscription = Sandbox.getInstance().getEngine().getAspectSubscriptionManager()
                .get(Aspect.all(NodeComponent.class));
        IntBag composites = subscription.getEntities();
        for (int composite : composites.getData()) {
            MainItemComponent mainItemComponent = SandboxComponentRetriever.get(composite, MainItemComponent.class);
            if (mainItemComponent.libraryLink.equals(link)) {
                result.add(composite);
            }
        }

        return result;
    }

    public static LayerItemVO getEntityLayer(int entity) {
        ZIndexComponent zIndexComponent = SandboxComponentRetriever.get(entity, ZIndexComponent.class);
        LayerMapComponent layerMapComponent = SandboxComponentRetriever.get(SandboxComponentRetriever.get(entity, ParentNodeComponent.class).parentEntity, LayerMapComponent.class);

        return layerMapComponent.getLayer(zIndexComponent.layerName);
    }

    /**
     * iterate over children recursively and do some operations
     *
     * @param root
     * @param action
     */
    public static void applyActionRecursivelyOnEntities(int root, Consumer<Integer> action) {
        action.accept(root);
        NodeComponent nodeComponent = SandboxComponentRetriever.get(root, NodeComponent.class);
        if (nodeComponent != null && nodeComponent.children != null) {
            for (int targetEntity : nodeComponent.children) {
                applyActionRecursivelyOnEntities(targetEntity, action);
            }
        }
    }

    public static void applyActionRecursivelyOnLibraryItems(CompositeItemVO rootCompositeItemVo, Consumer<CompositeItemVO> action) {
        action.accept(rootCompositeItemVo);
        if (rootCompositeItemVo.composite != null && rootCompositeItemVo.composite.sComposites.size() != 0) {
            for (CompositeItemVO currentCompositeItemVo : rootCompositeItemVo.composite.sComposites) {
                applyActionRecursivelyOnLibraryItems(currentCompositeItemVo, action);
            }
        }
    }

    public static void removeEntities(ArrayList<Integer> entityList) {
        for (int entity : entityList) {
            Sandbox.getInstance().getEngine().delete(entity);
        }
        Sandbox.getInstance().getEngine().process();
    }

    public static void refreshComponents(int entity) {
        TextureRegionComponent textureRegionComponent = SandboxComponentRetriever.get(entity, TextureRegionComponent.class);
        if (textureRegionComponent != null) {
            textureRegionComponent.scheduleRefresh();
        }

        PhysicsBodyComponent physicsBodyComponent = SandboxComponentRetriever.get(entity, PhysicsBodyComponent.class);
        if (physicsBodyComponent != null) {
            physicsBodyComponent.scheduleRefresh();
        }

        LightBodyComponent lightBodyComponent = SandboxComponentRetriever.get(entity, LightBodyComponent.class);
        if (lightBodyComponent != null) {
            lightBodyComponent.scheduleRefresh();
        }
    }

    public static int getEntityFromJson(String jsonString, int entityType, EntityFactory factory, int parent) {
        Json json = new Json();
        if(entityType == EntityFactory.COMPOSITE_TYPE) {
            CompositeItemVO vo = json.fromJson(CompositeItemVO.class, jsonString);
            return factory.createEntity(parent, vo);
        }
        if(entityType == EntityFactory.IMAGE_TYPE) {
            SimpleImageVO vo = json.fromJson(SimpleImageVO.class, jsonString);
            return factory.createEntity(parent, vo);
        }
        if(entityType == EntityFactory.NINE_PATCH) {
            Image9patchVO vo = json.fromJson(Image9patchVO.class, jsonString);
            return factory.createEntity(parent, vo);
        }
        if(entityType == EntityFactory.LABEL_TYPE) {
            LabelVO vo = json.fromJson(LabelVO.class, jsonString);
            return factory.createEntity(parent, vo);
        }
        if(entityType == EntityFactory.PARTICLE_TYPE) {
            ParticleEffectVO vo = json.fromJson(ParticleEffectVO.class, jsonString);
            return factory.createEntity(parent, vo);
        }
        if(entityType == EntityFactory.TALOS_TYPE) {
            TalosVO vo = json.fromJson(TalosVO.class, jsonString);
            return factory.createEntity(parent, vo);
        }
        if(entityType == EntityFactory.SPRITE_TYPE) {
            SpriteAnimationVO vo = json.fromJson(SpriteAnimationVO.class, jsonString);
            return factory.createEntity(parent, vo);
        }
        if(entityType == EntityFactory.SPINE_TYPE) {
            SpineVO vo = json.fromJson(SpineVO.class, jsonString);
            return factory.createEntity(parent, vo);
        }
        if(entityType == EntityFactory.COLOR_PRIMITIVE) {
            ColorPrimitiveVO vo = json.fromJson(ColorPrimitiveVO.class, jsonString);
            return factory.createEntity(parent, vo);
        }
        if(entityType == EntityFactory.LIGHT_TYPE) {
            LightVO vo = json.fromJson(LightVO.class, jsonString);
            return factory.createEntity(parent, vo);
        }
        return -1;
    }

    public static String getJsonStringFromEntity(int entity) {
        Json json = new Json();
        com.artemis.World engine = Sandbox.getInstance().getEngine();
        int entityType = SandboxComponentRetriever.get(entity, MainItemComponent.class).entityType;
        if(entityType == EntityFactory.COMPOSITE_TYPE) {
            CompositeItemVO vo = new CompositeItemVO();
            vo.loadFromEntity(entity, engine);
            return json.toJson(vo);
        }
        if(entityType == EntityFactory.IMAGE_TYPE) {
            SimpleImageVO vo = new SimpleImageVO();
            vo.loadFromEntity(entity, engine);
            return json.toJson(vo);
        }
        if(entityType == EntityFactory.NINE_PATCH) {
            Image9patchVO vo = new Image9patchVO();
            vo.loadFromEntity(entity, engine);
            return json.toJson(vo);
        }
        if(entityType == EntityFactory.LABEL_TYPE) {
            LabelVO vo = new LabelVO();
            vo.loadFromEntity(entity, engine);
            return json.toJson(vo);
        }
        if(entityType == EntityFactory.PARTICLE_TYPE) {
            ParticleEffectVO vo = new ParticleEffectVO();
            vo.loadFromEntity(entity, engine);
            return json.toJson(vo);
        }
        if(entityType == EntityFactory.TALOS_TYPE) {
            TalosVO vo = new TalosVO();
            vo.loadFromEntity(entity, engine);
            return json.toJson(vo);
        }
        if(entityType == EntityFactory.SPRITE_TYPE) {
            SpriteAnimationVO vo = new SpriteAnimationVO();
            vo.loadFromEntity(entity, engine);
            return json.toJson(vo);
        }
        if(entityType == EntityFactory.SPINE_TYPE) {
            SpineVO vo = new SpineVO();
            vo.loadFromEntity(entity, engine);
            return json.toJson(vo);
        }
        if(entityType == EntityFactory.COLOR_PRIMITIVE) {
            ColorPrimitiveVO vo = new ColorPrimitiveVO();
            vo.loadFromEntity(entity, engine);
            return json.toJson(vo);
        }
        if(entityType == EntityFactory.LIGHT_TYPE) {
            LightVO vo = new LightVO();
            vo.loadFromEntity(entity, engine);
            return json.toJson(vo);
        }
        return null;
    }

    public static String getJsonStringFromEntities(Set<Integer> entities) {
        CompositeVO holderComposite = new CompositeVO();
        com.artemis.World engine = Sandbox.getInstance().getEngine();
        for(int entity : entities) {
            int entityType = SandboxComponentRetriever.get(entity, MainItemComponent.class).entityType;
            if(entityType == EntityFactory.COMPOSITE_TYPE) {
                CompositeItemVO vo = new CompositeItemVO();
                vo.loadFromEntity(entity, engine);
                holderComposite.sComposites.add(vo);
            }
            if(entityType == EntityFactory.IMAGE_TYPE) {
                SimpleImageVO vo = new SimpleImageVO();
                vo.loadFromEntity(entity, engine);
                holderComposite.sImages.add(vo);
            }
            if(entityType == EntityFactory.NINE_PATCH) {
                Image9patchVO vo = new Image9patchVO();
                vo.loadFromEntity(entity, engine);
                holderComposite.sImage9patchs.add(vo);
            }
            if(entityType == EntityFactory.LABEL_TYPE) {
                LabelVO vo = new LabelVO();
                vo.loadFromEntity(entity, engine);
                holderComposite.sLabels.add(vo);
            }
            if(entityType == EntityFactory.PARTICLE_TYPE) {
                ParticleEffectVO vo = new ParticleEffectVO();
                vo.loadFromEntity(entity, engine);
                holderComposite.sParticleEffects.add(vo);
            }
            if(entityType == EntityFactory.TALOS_TYPE) {
                TalosVO vo = new TalosVO();
                vo.loadFromEntity(entity, engine);
                holderComposite.sTalosVFX.add(vo);
            }
            if(entityType == EntityFactory.SPRITE_TYPE) {
                SpriteAnimationVO vo = new SpriteAnimationVO();
                vo.loadFromEntity(entity, engine);
                holderComposite.sSpriteAnimations.add(vo);
            }
            if(entityType == EntityFactory.SPINE_TYPE) {
                SpineVO vo = new SpineVO();
                vo.loadFromEntity(entity, engine);
                holderComposite.sSpineAnimations.add(vo);
            }
            if(entityType == EntityFactory.COLOR_PRIMITIVE) {
                ColorPrimitiveVO vo = new ColorPrimitiveVO();
                vo.loadFromEntity(entity, engine);
                holderComposite.sColorPrimitives.add(vo);
            }
            if(entityType == EntityFactory.LIGHT_TYPE) {
                LightVO vo = new LightVO();
                vo.loadFromEntity(entity, engine);
                holderComposite.sLights.add(vo);
            }
        }

        Json json = new Json();
        String result = json.toJson(holderComposite);

        return result;
    }
}
