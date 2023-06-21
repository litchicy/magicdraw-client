package DTSystem.Function.XMLGeneration;

import java.util.Date;
import java.text.SimpleDateFormat;

public class BaseInf
{
    private final String userID;
    private final String modelType;
    private final String ip;
    private final boolean update;
    private final Date date;

    public BaseInf(String userID, String modelType, String ip, boolean update)
    {
        this.userID = userID;
        this.modelType = modelType;
        this.ip = ip;
        this.update = update;
        this.date = new Date();
    }

    public String getUserID()
    {
        return userID;
    }

    public String getModelType()
    {
        return modelType;
    }

    public String getIp()
    {
        return ip;
    }

    public boolean getUpdate()
    {
        return update;
    }

    public String getDate()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        return formatter.format(this.date);
    }
}