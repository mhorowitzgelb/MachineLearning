import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.gui.chart.traces.painters.TracePainterFill;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.pmml.jaxbbindings.ROC;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by mhorowitzgelb on 2/14/2015.
 */
public class Graphing {
    static double classRatio;
    static Instances trainingInstances;
    static Instances testingInstances;
    static ArrayList<Instance> classOneInstances;
    static ArrayList<Instance> classTwoInstances;
    public static void main(String[] args) throws Exception {
        DataSource learningSource = new DataSource("C:\\Users\\mhorowitzgelb\\Desktop\\_HW2\\lymph_train.arff");
        trainingInstances = learningSource.getDataSet();
        DataSource testingSource = new DataSource("C:\\Users\\mhorowitzgelb\\Desktop\\_HW2\\lymph_test.arff");
        testingInstances = testingSource.getDataSet();
        trainingInstances.setClassIndex(trainingInstances.numAttributes() -1);
        testingInstances.setClassIndex(testingInstances.numAttributes() -1);

        classOneInstances = new ArrayList<Instance>();
        classTwoInstances = new ArrayList<Instance>();
        for(Instance instance : trainingInstances){
            if(instance.stringValue(instance.classAttribute()).equals(trainingInstances.classAttribute().value(0)))
                classOneInstances.add(instance);
            else if(instance.stringValue(instance.classAttribute()).equals(instance.classAttribute().value(1)))
                classTwoInstances.add(instance);
            else{
                System.out.println("What the fucl");
                System.exit(0);
            }
        }

        classRatio = ((double) classOneInstances.size())/ trainingInstances.size();

        Chart2D learningCurve = new Chart2D();
        learningCurve.getAxisX().setAxisTitle(new IAxis.AxisTitle("Training Size"));
        learningCurve.getAxisY().setAxisTitle(new IAxis.AxisTitle("Accuracy"));
        ITrace2D naiveBayesCurve = new Trace2DSimple();
        ITrace2D tanCurve = new Trace2DSimple();
        naiveBayesCurve.setColor(Color.BLUE);
        naiveBayesCurve.setName("Naive Bayes");
        tanCurve.setColor(Color.RED);
        tanCurve.setName("TAN");
        learningCurve.addTrace(naiveBayesCurve);
        learningCurve.addTrace(tanCurve);


        int[] sizes = new int[]{25,50, 100};
        for(int size : sizes){
            List<Tree> naiveTrees = new ArrayList<Tree>();
            List<Tree> tanTrees = new ArrayList<Tree>();
            for(int i = 0; i < 4; i ++){
                naiveTrees.add(StratifiedTree(size,false));
                tanTrees.add(StratifiedTree(size,true));
            }
            double naiveAverage = 0;
            for(Tree tree : naiveTrees){
                naiveAverage += GetAccuracy(tree);
            }
            naiveAverage /= 4;

            double tanAverage = 0;
            for(Tree tree : tanTrees){
                tanAverage += GetAccuracy(tree);
            }
            tanAverage /= 4;
            naiveBayesCurve.addPoint(size , naiveAverage);
            tanCurve.addPoint(size, tanAverage);
        }

        JFrame frame = new JFrame();
        frame.getContentPane().add(learningCurve);
        frame.setSize(1000, 500);
        frame.setVisible(true);


        Chart2D rocChart = new Chart2D();
        rocChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("False Positive Rate"));
        rocChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("True Positive Rate"));

        ITrace2D rocNaiveCurve = new Trace2DSimple();
        //rocNaiveCurve.setTracePainter(new TracePainterDisc());
        rocNaiveCurve.setColor(Color.BLUE);
        rocNaiveCurve.setName("Naive Bayes");
        ITrace2D rocTanCurve = new Trace2DSimple();
        //rocTanCurve.setTracePainter(new TracePainterDisc());
        rocTanCurve.setName("TAN");
        rocTanCurve.setColor(Color.red);
        rocChart.addTrace(rocNaiveCurve);
        rocChart.addTrace(rocTanCurve);




        Tree naiveBayesTree = new Tree(trainingInstances,false);
        Tree tanTree = new Tree(trainingInstances, true);
        List<ROCInstance> naiveRocInstances = new ArrayList<ROCInstance>();
        List<ROCInstance> tanRocInstances = new ArrayList<ROCInstance>();
        int class2 = 0;
        int class1 = 0;
        for(Instance instance : testingInstances){
            if(instance.stringValue(instance.classAttribute()).equals(instance.classAttribute().value(1)))
                class2 ++;
            else
                class1 ++;
            naiveRocInstances.add(new ROCInstance(instance, naiveBayesTree.Classify(instance)));
            tanRocInstances.add(new ROCInstance(instance, tanTree.Classify(instance)));
        }
        Collections.sort(naiveRocInstances);
        Collections.sort(tanRocInstances);

        for(int confidenceIndexThreshold = 0; confidenceIndexThreshold < naiveRocInstances.size(); confidenceIndexThreshold ++){
            double falsePositiveNaive = 0;
            double truePositiveNaive = 0;
            double falsePositiveTan = 0;
            double truePositiveTan = 0;
            for(int j =0; j < confidenceIndexThreshold; j ++){
                Instance naiveInstance = naiveRocInstances.get(j).instance;
                Instance tanInstance = tanRocInstances.get(j).instance;
                if(naiveInstance.stringValue(naiveInstance.classAttribute()).equals(naiveInstance.classAttribute().value(1))) {
                    truePositiveNaive++;
                }
                else {
                    falsePositiveNaive++;
                }

                if(tanInstance.stringValue(tanInstance.classAttribute()).equals(tanInstance.classAttribute().value(1))) {
                    truePositiveTan++;
                }
                else {
                    falsePositiveTan++;
                }
            }
            //rocNaiveCurve.addPoint(falsePositiveNaive/ class1, truePositiveNaive/class2);
            rocTanCurve.addPoint(falsePositiveTan / class1, truePositiveTan / class2);
        }

        JFrame rocFrame = new JFrame();
        rocFrame.getContentPane().add(rocChart);
        rocFrame.setSize(1000,500);
        rocFrame.setVisible(true);
    }

    public static Tree StratifiedTree(int size, boolean tan){


        int numClassOne = (int)(size * classRatio);

        if(size == 100)
            return new Tree(trainingInstances,tan);

        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        for(Enumeration<Attribute> e = trainingInstances.enumerateAttributes(); e.hasMoreElements(); )
            attributeList.add(e.nextElement());
        Instances instances = new Instances(testingInstances);
        instances.clear();
        ArrayList<Instance> classOneCopy = new ArrayList<Instance>(classOneInstances);
        ArrayList<Instance> classTwoCopy = new ArrayList<Instance>(classTwoInstances);

        for(int i = 0; i < numClassOne; i ++){
            instances.add(classOneCopy.remove((int)(Math.random() * classOneCopy.size())));
        }

        for(int i = 0; i < size - numClassOne; i ++){
            instances.add(classTwoCopy.remove((int)(Math.random() * classTwoCopy.size())));
        }

        return new Tree(instances, tan);
    }

    public static double GetAccuracy(Tree tree) throws Exception {
        double correct = 0;
        for(Instance instance : testingInstances){
            double probability = tree.Classify(instance);
            if(probability > 0.5 && instance.stringValue(instance.classAttribute()).equals(instance.classAttribute().value(0)))
                correct ++;
            else if(probability <= 0.5 && instance.stringValue(instance.classAttribute()).equals(instance.classAttribute().value(1)))
                correct ++;
        }
        return correct / testingInstances.size();
    }

    public static class ROCInstance implements Comparable<ROCInstance>{
        double classTwoProbability;
        Instance instance;

        public ROCInstance(Instance instance, double classTwoProbability){
            this.instance = instance;
            this.classTwoProbability = classTwoProbability;
        }

        @Override
        public int compareTo(ROCInstance o) {
            if(o.classTwoProbability > this.classTwoProbability){
                return -1;
            }
            else if(o.classTwoProbability == this.classTwoProbability)
                return 0;
            else
                return 1;
        }

        @Override
        public String toString(){
            return classTwoProbability + " " + instance.stringValue(instance.classAttribute());
        }
    }

}
