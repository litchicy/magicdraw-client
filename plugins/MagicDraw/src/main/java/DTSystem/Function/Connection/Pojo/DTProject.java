package DTSystem.Function.Connection.Pojo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;

public class DTProject
{
    private final String projectID;
    private final String projectName;
    private final ArrayList<DTModel> modelList = new ArrayList<>();

    public DTProject(JSONObject jsonObject)
    {
        this.projectID = jsonObject.getString("project_id");
        this.projectName = jsonObject.getString("project_name");

        JSONArray models = jsonObject.getJSONArray("models");
        for(int i = 0; i < models.size(); i++)
        {
            JSONObject model = (JSONObject)models.get(i);
            String modelID = model.getString("model_id");
            String modelName = model.getString("model_name");
            modelList.add(new DTModel(modelID, modelName));
        }
    }

    public String getProjectID()
    {
        return this.projectID;
    }

    public String getProjectName()
    {
        return this.projectName;
    }

    public ArrayList<DTModel> getModelList()
    {
        return this.modelList;
    }
}
