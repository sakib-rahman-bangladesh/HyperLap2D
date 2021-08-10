package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.components.SpineDataComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.renderer.data.SpineVO;
import games.rednblack.editor.renderer.utils.Version;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.extention.spine.SpineItemType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpineAsset extends Asset {
    @Override
    protected boolean matchMimeType(FileHandle file) {
        try {
            String contents = FileUtils.readFileToString(file.file(), "utf-8");
            return file.extension().equalsIgnoreCase("json")
                    && (contents.contains("\"skeleton\":{")
                        || contents.contains("\"skeleton\": {")
                        || contents.contains("{\"bones\":["));
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public int getType() {
        return ImportUtils.TYPE_SPINE_ANIMATION;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.SPINE_DIR_PATH + File.separator + file.nameWithoutExtension() + File.separator +
                    file.nameWithoutExtension() + ".json");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        for (FileHandle handle : new Array.ArrayIterator<>(files)) {
            File copiedFile = importExternalAnimationIntoProject(handle, progressHandler);
            if (copiedFile == null)
                continue;

            if (copiedFile.getName().toLowerCase().endsWith(".json")) {
                resolutionManager.rePackProjectImagesForAllResolutionsSync();
            }
        }
    }

    @Override
    public boolean deleteAsset(int root, String spineName) {
        for (ResolutionEntryVO resolutionEntryVO : projectManager.getCurrentProjectInfoVO().resolutions) {
            if(!deleteSpineAnimation(resolutionEntryVO.name, spineName))
                return false;
        }

        if (deleteSpineAnimation("orig", spineName)) {
            postDeleteSpineAnimation(root, spineName);
            return true;
        }

        return false;
    }

    private File importExternalAnimationIntoProject(FileHandle animationFileSource, ProgressHandler progressHandler) {
        try {
            String fileName = animationFileSource.name();
            if (!HyperLap2DUtils.JSON_FILTER.accept(null, fileName)) {
                return null;
            }

            String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
            String sourcePath;
            String animationDataPath;
            String targetPath;
            if (HyperLap2DUtils.JSON_FILTER.accept(null, fileName)) {
                sourcePath = animationFileSource.path();

                animationDataPath = FilenameUtils.getFullPathNoEndSeparator(sourcePath);
                targetPath = projectManager.getCurrentProjectPath() + "/assets/orig/spine-animations" + File.separator + fileNameWithOutExt;
                FileHandle atlasFileSource = new FileHandle(animationDataPath + File.separator + fileNameWithOutExt + ".atlas");
                if (!atlasFileSource.exists()) {
                    Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                            "\nCould not find '" + atlasFileSource.name() +"'.\nCheck if the file exists in the same directory.").padBottom(20).pack();
                    return null;
                }

                TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(atlasFileSource, atlasFileSource.parent(), false);
                for (TextureAtlas.TextureAtlasData.Page imageFile : new Array.ArrayIterator<>(atlas.getPages())) {
                    if (!imageFile.textureFile.exists()) {
                        Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                                "\nCould not find " + imageFile.textureFile.name() + ".\nCheck if the file exists in the same directory.").padBottom(20).pack();
                        return null;
                    }
                }

                Version spineVersion = getSpineVersion(animationFileSource);
                if (spineVersion.compareTo(SpineItemType.SUPPORTED_SPINE_VERSION) < 0) {
                    Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                            "\nCould not import Spine Animation.\nRequired version >=" + SpineItemType.SUPPORTED_SPINE_VERSION.get() + " found " + spineVersion.get()).padBottom(20).pack();
                    return null;
                }

                File targetDir = new File(targetPath);
                FileUtils.forceMkdir(targetDir);
                FileUtils.copyFileToDirectory(atlasFileSource.file(), targetDir);
                for (TextureAtlas.TextureAtlasData.Page imageFile : new Array.ArrayIterator<>(atlas.getPages())) {
                    FileUtils.copyFileToDirectory(imageFile.textureFile.file(), targetDir);
                }
                File jsonFileTarget = new File(targetPath + File.separator + fileNameWithOutExt + ".json");

                FileUtils.copyFile(animationFileSource.file(), jsonFileTarget);
                FileHandle tmpDir = new FileHandle(projectManager.getCurrentProjectPath() + File.separator + "tmp");
                if (tmpDir.exists())
                    FileUtils.forceDelete(tmpDir.file());
                FileUtils.forceMkdir(tmpDir.file());
                ImportUtils.unpackAtlasIntoTmpFolder(atlasFileSource.file(), fileNameWithOutExt, tmpDir.path());
                Array<FileHandle> images = new Array<>(tmpDir.list());
                projectManager.copyImageFilesForAllResolutionsIntoProject(images, true, progressHandler);
                FileUtils.forceDelete(tmpDir.file());

                for (TextureAtlas.TextureAtlasData.Region region : new Array.ArrayIterator<>(atlas.getRegions())) {
                    projectManager.getCurrentProjectInfoVO().animationsPacks.get("main").regions.add(fileNameWithOutExt+region.name);
                }

                return jsonFileTarget;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Version getSpineVersion(FileHandle fileHandle) {
        Version version;

        String regex = "\"spine\" *: *\"(\\d+\\.\\d+\\.?\\d*)\"";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fileHandle.readString());

        if (matcher.find()) {
            version = new Version(matcher.group(1));
        } else {
            version = new Version("0.0.0");
        }

        return version;
    }

    private boolean deleteSpineAnimation(String resolutionName, String spineName) {
        String spinePath = projectManager.getCurrentProjectPath() + "/assets/" + resolutionName + "/spine-animations" + File.separator;
        String filePath = spinePath + spineName;
        FileHandle jsonPath = new FileHandle(filePath + File.separator + spineName + ".json");

        JsonValue root = new JsonReader().parse(jsonPath);
        for (JsonValue skinMap = root.getChild("skins"); skinMap != null; skinMap = skinMap.next) {
            for (JsonValue slotEntry = skinMap.getChild("attachments"); slotEntry != null; slotEntry = slotEntry.next) {
                for (JsonValue entry = slotEntry.child; entry != null; entry = entry.next) {
                    String name = spineName + entry.getString("name", entry.name);
                    deleteSingleImage(resolutionName, name);
                    projectManager.deleteRegionFromPack(projectManager.getCurrentProjectInfoVO().animationsPacks, name);
                }
            }
        }
        return ImportUtils.deleteDirectory(filePath);
    }

    private boolean deleteSingleImage(String resolutionName, String imageName) {
        String imagesPath = projectManager.getCurrentProjectPath() + "/assets/" + resolutionName + "/images" + File.separator;
        String filePath = imagesPath + imageName + ".png";
        projectManager.deleteRegionFromPack(projectManager.getCurrentProjectInfoVO().imagesPacks, imageName);
        if (!(new File(filePath)).delete()) {
            filePath = imagesPath + imageName + ".9.png";
            return (new File(filePath)).delete();
        }
        return true;
    }

    protected void postDeleteSpineAnimation(int root, String spineAnimationName) {
        deleteEntitiesWithSpineAnimation(root, spineAnimationName);
        deleteAllItemsSpineAnimations(spineAnimationName);
    }

    private void deleteAllItemsSpineAnimations(String spineAnimationName) {
        for (CompositeItemVO compositeItemVO : projectManager.getCurrentProjectInfoVO().libraryItems.values()) {
            deleteAllSpineAnimationsOfItem(compositeItemVO, spineAnimationName);
        }
    }

    private void deleteAllSpineAnimationsOfItem(CompositeItemVO compositeItemVO, String spineAnimationName) {
        Consumer<CompositeItemVO> action = (rootItemVo) -> deleteCurrentItemSpineAnimations(rootItemVo, spineAnimationName);
        EntityUtils.applyActionRecursivelyOnLibraryItems(compositeItemVO, action);
    }

    private void deleteCurrentItemSpineAnimations(CompositeItemVO compositeItemVO, String spineAnimationName) {
        tmpImageList.clear();
        if (compositeItemVO.composite != null && compositeItemVO.composite.sSpineAnimations.size() != 0) {
            ArrayList<SpineVO> spineAnimations = compositeItemVO.composite.sSpineAnimations;
            tmpImageList.addAll(spineAnimations
                    .stream()
                    .filter(spineVO -> spineVO.animationName.equals(spineAnimationName))
                    .collect(Collectors.toList()));
            spineAnimations.removeAll(tmpImageList);
        }
    }

    private void deleteEntitiesWithSpineAnimation(int rootEntity, String spineName) {
        tmpEntityList.clear();
        Consumer<Integer> action = (root) -> {
            SpineDataComponent spineDataComponent = SandboxComponentRetriever.get(root, SpineDataComponent.class);
            if (spineDataComponent != null && spineDataComponent.animationName.equals(spineName)) {
                tmpEntityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(tmpEntityList);
    }
}
