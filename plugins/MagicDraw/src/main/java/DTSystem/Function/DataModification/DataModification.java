package DTSystem.Function.DataModification;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.ClassTypes;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.impl.ModelHierarchyVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class DataModification
{
    private final String elementID;
    private final String attribute;
    private final String newValue;
    private final ModelHierarchyVisitor visitor = new ModelHierarchyVisitor();
    private static final String[] namedElement = {"Activity", "Diagram"};

    public DataModification(String elementID, String attribute, String newValue)
    {
        this.elementID = elementID;
        this.attribute = attribute;
        this.newValue = newValue;
    }

    public boolean modify()
    {
        boolean isModified = false;
        Project project = Application.getInstance().getProject();
        if(project != null)
        {
            System.out.println("已找到项目");
            // 获取模型根元素
            Package root = project.getPrimaryModel();
            Element[] elements = root.getOwnedElement().toArray(new Element[0]);
            Element rootElement = elements[1];

            ArrayList<Element> allElements = getAllElement(rootElement);
            Element element = findElement(allElements, this.elementID);
            if(element != null)
            {
                System.out.println("已找到模型根元素");
                String classType = ClassTypes.getShortName(element.getClassType());  // 模型元素的类型
                System.out.println(classType);
                boolean flag = isNamedElement(classType);
                if(flag)
                {
                    System.out.println("属于NamedElement");
                    NamedElement namedElement = (NamedElement)element;
                    switch(this.attribute)
                    {
                        case "name":
                            namedElement.setName(this.newValue);
                            break;
                    }
                    isModified =  true;
                }
                else
                {
                    System.out.println("不属于NamedElement");
                }
            }
            else
            {
                System.out.println("找不到模型根元素");
                isModified =  false;
            }
        }
        else
        {
            System.out.println("找不到项目");
            isModified =  false;
        }
        return isModified;
    }

    /**
     * Goes through all children of given model elements.
     * Demonstrates way how to collect all children by using FOR cycle and avoiding recursion.
     * Visit every child with StatisticsVisitor.
     *
     * @param root the root model element.
     */
    private ArrayList<Element> getAllElement(Element root)
    {
        ArrayList<Element> all = new ArrayList<>();
        all.add(root);

        // if current element has children, list will be increased.
        for (int i = 0; i < all.size(); i++)
        {
            Element current = all.get(i);
            try
            {
                // let's perform some action with this element in visitor.
                current.accept(visitor);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            // add all children into end of this list, so it emulates recursion.
            Collection<Element> childrenElement = current.getOwnedElement();
            all.addAll(childrenElement);
        }
        return all;
    }

    // 根据ID查找对应的模型元素
    public Element findElement(ArrayList<Element> elements, String ID)
    {
        Element element = null;
        for(int i = 0; i < elements.size(); i++)
        {
            Element e = elements.get(i);
            if(Objects.equals(ID, e.getID()))
            {
                element = e;
                break;
            }
        }
        return element;
    }

    // 判断模型元素类型是否属于NamedElement
    private boolean isNamedElement(String classType)
    {
        boolean flag = false;
        for(int i = 0; i < namedElement.length; i++)
        {
            if (classType.equals(namedElement[i]))
            {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
