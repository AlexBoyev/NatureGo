package mindcet.natureg.Map;

public class HikeHistoryItem {

    private int ImageResource;
    private String hikeName;
    private String hikeData;

    HikeHistoryItem(int ImageResource, String hikeName, String hikeData){
        this.ImageResource = ImageResource;
        this.hikeName = hikeName;
        this.hikeData = hikeData;
    }
    public int getImageResource() {
        return ImageResource;
    }

    public String getHikeName() {
        return hikeName;
    }

    public String getHikeData() {
        return hikeData;
    }
}
