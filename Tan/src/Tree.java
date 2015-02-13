import org.w3c.dom.Attr;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * Created by mhorowitzgelb on 2/13/2015.
 */
public class Tree {


    public final double LOG_OF_2 = Math.log(2);
    public ClassNode classNode;
    public Instances learningInstances;

    public Tree(Instances learningInstances){
        this.learningInstances = learningInstances;

        this.classNode = new ClassNode(learningInstances.classAttribute());
        for(Enumeration<Attribute> e = learningInstances.enumerateAttributes(); e.hasMoreElements();){
            Attribute attribute = e.nextElement();
            if(!learningInstances.classAttribute().equals(attribute)){
                NonClassNode node = new NonClassNode(attribute);
                node.parents.add(classNode);
                classNode.children.add(node);
            }
        }
        Prim();
    }

    public double MutualInformation(Instances instances, Attribute a, Attribute b){
        double sum = 0;
        for(int i = 0; i < a.numValues(); i ++){
            String aValue = a.value(i);
            for(int j = 0; j < b.numValues(); j ++){
                String bValue = b.value(j);
                HashMap<Attribute, String> abValueMap = new HashMap<Attribute, String>();
                abValueMap.put(a, aValue);
                abValueMap.put(b, bValue);
                double pAB = Count(instances, abValueMap) / instances.numInstances();

                HashMap<Attribute, String> aValueMap = new HashMap<Attribute, String>();
                aValueMap.put(a,aValue);
                double pA = Count(instances, aValueMap) / instances.numInstances();

                HashMap<Attribute, String> bValueMap = new HashMap<Attribute, String>();
                bValueMap.put(b, bValue);
                double pB = Count(instances, bValueMap) / instances.numInstances();


                if(pAB != 0)
                    sum += pAB * Math.log(pAB/(pA*pB)) / LOG_OF_2;
            }
        }
        return sum;
    }


    public double Count(Instances instances, Map<Attribute , String> valueMap){
        double count = 0;
        for(Instance instance : instances){
            boolean match = true;
            for(Attribute attribute : valueMap.keySet()){
                if(!instance.stringValue(attribute).equals(valueMap.get(attribute))){
                    match = false;
                    break;
                }
            }
            if(match){
                count ++;
            }
        }
        return count;
    }

    public void Prim(){
        List<NonClassNode> vNew = new ArrayList<NonClassNode>();
        List<NonClassNode> v = new ArrayList<NonClassNode>(classNode.children);
        vNew.add(v.remove(0));
        while(v.size() > 0) {
            double maxMutualInfo = Double.NEGATIVE_INFINITY;
            NonClassNode maxEdgeA = null;
            NonClassNode maxEdgeB = null;
            for (NonClassNode aNode : vNew){
                for (NonClassNode bNode : v) {
                    double mutualInfo = MutualInformation(learningInstances,aNode.attribute, bNode.attribute);
                    if(mutualInfo > maxMutualInfo){
                        maxMutualInfo = mutualInfo;
                        maxEdgeA = aNode;
                        maxEdgeB = bNode;
                    }
                    else if(mutualInfo == maxMutualInfo){
                        if(AttributeIndex(aNode.attribute) < AttributeIndex(maxEdgeA.attribute)){
                            maxMutualInfo = mutualInfo;
                            maxEdgeA = aNode;
                            maxEdgeB = bNode;
                        }
                        else if(aNode.attribute.equals(maxEdgeA.attribute) && AttributeIndex(bNode.attribute) < AttributeIndex(maxEdgeB.attribute)){
                            maxMutualInfo = mutualInfo;
                            maxEdgeA = aNode;
                            maxEdgeB = bNode;
                        }
                    }
                }
            }
            maxEdgeA.children.add(maxEdgeB);
            v.remove(maxEdgeB);
            vNew.add(maxEdgeB);
        }

    }

    private int AttributeIndex( Attribute attribute){
        for(int i = 0; i < learningInstances.numAttributes(); i ++){
            if(learningInstances.attribute(i).equals(attribute))
                return i;
        }
        return -1;
    }
}
