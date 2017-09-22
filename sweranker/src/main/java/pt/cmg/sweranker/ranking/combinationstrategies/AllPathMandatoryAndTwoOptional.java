package pt.cmg.sweranker.ranking.combinationstrategies;


import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.AnnualClassCombination;


/**
 * This is slightly more complicated algorithm class to calculate the Degree Class Annual Combinations.
 * Originally developed for the Degree (Universidade de Coimbra - LEI + MEI), this is a departure from the
 * simpler ones previously developed for the (Universidade do Porto - MiEIC), in the way that this algorithm
 * was the first developed to deal with Multi Path Degree Years.
 * <p>
 * While most degrees have a straightforward structure, with a common trunk of classes in the first years
 * and then the more advanced years with mandatory classes plus a choice of sme optionals, there are those
 * like Coimbra and Minho that split their advanced years into "Paths". These are nothing more than pre-built
 * sets of classes of a given theme that must be taken together. And these paths are likely mutually exclusive,
 * i.e. if you take one, you cannot take another.
 * </p>
 * <p>
 * However, Coimbra goes even further by adding some optional classes to the pre-built roaster. And that
 * really just means that the way to calculate the annual combinations gets really complicated.
 * </p>
 * <p>
 * In particular, this algorithm class assumes the following:<br/>
 * 1 - This year has multiple paths, it can be anything upwards of 1 <br/>
 * 2 - Each path, for that year, has mandatory classes shared by every path <br/>
 * 3 - Each path, for that year, has mandatory classes unique to that path <br/>
 * 4 - Each path, for that year, has optional classes that are shared among every path <br/>
 * 5 - Each path, for that year, has optional classes that were mandatory in another path, but since they are not
 * exclusive, can also be used on another.
 * 6 - The number of optional classes tha can be added to the mandatory ones is TWO!
 * </p>
 */
public class AllPathMandatoryAndTwoOptional implements ClassCombinationStrategy {


    private static String combinationIdBase;
    private static int idCounter;
    private static int currentYear;

    @Override
    public List<AnnualClassCombination> getAnnualClassCombinations(int year, List<DegreeClass> allDegreeClasses) {

        currentYear = year;
        combinationIdBase = "d_" + allDegreeClasses.get(0).getDegreeId() + "_y_" + year + "_c_";
        idCounter = 1;

        List<AnnualClassCombination> allAnnualCombinations = new ArrayList<>();
        List<DegreeClass> classesOfYear = getDegreeClassesOfYear(allDegreeClasses);

        Set<Integer> availablePaths = getAvailablePaths(classesOfYear);

        for (Integer path : availablePaths) {

            List<String> mandatoryClasses = getMandatoryPathClasses(path, classesOfYear);
            List<String> optionalClasses = getOptionalPathClasses(path, classesOfYear);

            allAnnualCombinations.addAll(generateAnnualCombinations(mandatoryClasses, optionalClasses));

        }

        return allAnnualCombinations;
    }


    /**
     * This function pretty much just returns a list with all the Degree Classes for THE CURRENT YEAR
     * we are trying to get the combinations for.
     */
    private List<DegreeClass> getDegreeClassesOfYear(List<DegreeClass> allDegreeClasses) {
        List<DegreeClass> classesOfYear = new ArrayList<>();
        for (DegreeClass degreeClass : allDegreeClasses) {
            if (degreeClass.getYear() == currentYear) {
                classesOfYear.add(degreeClass);
            }
        }
        return classesOfYear;
    }

    /**
     * Returns the Set with all the available paths for this year's classes.
     * Some degrees have different paths to complete it and this is an approach to it.
     *
     * @param allClasses
     * @return
     */
    private Set<Integer> getAvailablePaths(List<DegreeClass> allClasses) {

        Set<Integer> paths = new TreeSet<>();
        // Atenção a esta instrução, isto vai os arrays de todas as cadeiras do ano para inserir multiplas vezes
        // os mesmos paths, como é um SET não haverá repetidos, mas mesmo assim vai tudo lá para dentro
        for (DegreeClass degreeClass : allClasses) {
            for (int i = 0; i < degreeClass.getPaths().length; i++) {
                paths.add(degreeClass.getPaths()[i]);
            }
        }
        return paths;
    }

    /**
     * Returns a list with all the MANDATORY classes for the given PATH.
     * This is very important, because the choosing of these classes are determinant
     * to the combinations generated.
     */
    private List<String> getMandatoryPathClasses(int path, List<DegreeClass> allClasses) {

        List<String> mandatoryClasses = new ArrayList<>();
        for (DegreeClass degreeClass : allClasses) {
            if (degreeClass.isMandatoryClass()) {
                mandatoryClasses.add(degreeClass.getId());
            }

            if (degreeClass.isPathMandatory(path)) {
                mandatoryClasses.add(degreeClass.getId());
            }
        }
        return mandatoryClasses;
    }


    /**
     * Returns the optional classes for this path. This is normally slightly more complicated in
     * PATH aware degrees because some classes need to be picked here and there.
     * In any case this is the list that will suffer Combinatory math a bit further down the line
     * to effectively create the final combinations of the year.
     */
    private List<String> getOptionalPathClasses(int path, List<DegreeClass> allClasses) {

        List<String> optionalClasses = new ArrayList<>();
        for (DegreeClass degreeClass : allClasses) {
            if (degreeClass.isOptionalForPath(path)) {
                optionalClasses.add(degreeClass.getId());
            }

        }
        return optionalClasses;
    }


    private List<AnnualClassCombination> generateAnnualCombinations(List<String> mandatoryClasses, List<String> optionalClasses) {

        ICombinatoricsVector<String> optionalVector = Factory.createVector(optionalClasses.toArray(new String[optionalClasses.size()]));

        // creates the generator that can calculate the needed combinations.
        Generator<String> optionalCombinations = Factory.createSimpleCombinationGenerator(optionalVector, 2);


        List<AnnualClassCombination> resultingCombinations = new ArrayList<>();


        // Now iterate over all the possible combinations, add them the mandatory classes and add them to the final object.
        for (ICombinatoricsVector<String> comboVector : optionalCombinations.generateAllObjects()) {

            AnnualClassCombination currentCombination = new AnnualClassCombination(combinationIdBase + idCounter);
            currentCombination.setYear(currentYear);

            currentCombination.addDegreeClasses(comboVector.getVector());
            currentCombination.addDegreeClasses(mandatoryClasses);

            resultingCombinations.add(currentCombination);

            // increment counter to create a new ID for this particular combo
            idCounter++;
        }

        return resultingCombinations;
    }
}
