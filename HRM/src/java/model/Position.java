



package model;





public class Position {

    private int positionId;
    private String positionName;
    private int level;
    private String description;

    public Position() {}

    public Position(int positionId, String positionName, int level, String description) {
        this.positionId = positionId;
        this.positionName = positionName;
        this.level = level;
        this.description = description;
    }

    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
