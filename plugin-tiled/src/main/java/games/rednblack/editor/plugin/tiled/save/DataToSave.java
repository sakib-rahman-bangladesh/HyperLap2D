package games.rednblack.editor.plugin.tiled.save;

import java.util.stream.StreamSupport;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import games.rednblack.editor.plugin.tiled.data.ParameterVO;
import games.rednblack.editor.plugin.tiled.data.TileVO;

/**
 * Created by mariam on 3/23/16.
 */
public class DataToSave {

    private Array<TileVO> tiles;
    private ParameterVO parameterVO;

    public DataToSave() {
        tiles = new Array<>();
        parameterVO = new ParameterVO();
    }

    public void addTile(String tileDrawableName, int type) {
        TileVO newTile = new TileVO(tileDrawableName);
        newTile.entityType = type;
        if (!tiles.contains(newTile, false)) {
            tiles.add(newTile);
        }
    }

    public void removeTile(String tileDrawableName) {
        tiles.forEach(tile -> {
            if (tile.regionName.equals(tileDrawableName)) {
                tiles.removeValue(tile, false);
            }
        });

    }
    
    /**
     * Removes all tiles.
     */
    public void removeAllTiles() {
    	tiles.clear();
    }

    public void setTileGridOffset(TileVO tileVO) {
        StreamSupport.stream(tiles.spliterator(), false)
                .filter(tile -> tile.regionName.equals(tileVO.regionName))
                .findFirst()
                .ifPresent(t -> t.gridOffset = tileVO.gridOffset);
    }

    public Vector2 getTileGridOffset(String regionName) {
        return StreamSupport.stream(tiles.spliterator(), false)
                .filter(tile -> tile.regionName.equals(regionName))
                .findFirst()
                .get()
                .gridOffset;
    }

    public TileVO getTile(String regionName) {
        return StreamSupport.stream(tiles.spliterator(), false)
                .filter(tile -> tile.regionName.equals(regionName))
                .findFirst()
                .get();
    }

    public Array<TileVO> getTiles() {
        return tiles;
    }

    public boolean containsTile(String regionName) {
        return StreamSupport.stream(tiles.spliterator(), false).anyMatch(tile -> tile.regionName.equals(regionName));
    }

    public ParameterVO getParameterVO() {
        return parameterVO;
    }

    public void setParameterVO(ParameterVO parameterVO) {
        this.parameterVO = parameterVO;
    }

    public void setGrid(float gridWidth, float gridHeight) {
        parameterVO.gridWidth = gridWidth;
        parameterVO.gridHeight = gridHeight;
    }
}
