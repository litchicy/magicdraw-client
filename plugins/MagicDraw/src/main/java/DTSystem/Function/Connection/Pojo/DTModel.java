package DTSystem.Function.Connection.Pojo;

public class DTModel
{
    private final String modelID;
    private final String modelName;

    public DTModel(String modelID, String modelName)
    {
        this.modelID = modelID;
        this.modelName = modelName;
    }

    public String getModelID()
    {
        return this.modelID;
    }

    public String getModelName()
    {
        return this.modelName;
    }
}
