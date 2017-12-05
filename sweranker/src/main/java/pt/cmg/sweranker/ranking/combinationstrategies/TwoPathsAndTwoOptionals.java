package pt.cmg.sweranker.ranking.combinationstrategies;

import com.google.common.collect.Sets;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.AnnualClassCombination;

/**
 * Created by Carlos on 05/12/2017.
 */

public class TwoPathsAndTwoOptionals implements ClassCombinationStrategy {


    private static String combinationIdBase;
    private static int idCounter;
    private static int currentYear;

    private static Set<Integer> availablePaths;
    private static Map<Integer, Set<String>> mandatoryDegreeClassesByPath;
    private static Map<Integer, Set<String>> optionalDegreeClassesByPath;
    private static Collection<String> mandatoryClasses;


    @Override
    public List<AnnualClassCombination> getAnnualClassCombinations(int targetYear, List<DegreeClass> allDegreeClasses) {

        currentYear = targetYear;
        combinationIdBase = "d_" + allDegreeClasses.get(0).getDegreeId() + "_y_" + targetYear + "_c_";
        idCounter = 1;

        List<DegreeClass> classesOfYear = getDegreeClassesOfYear(allDegreeClasses);

        availablePaths = getAvailablePaths(classesOfYear);
        mandatoryDegreeClassesByPath = getClassesByPathView(classesOfYear);
        optionalDegreeClassesByPath = getOptionalClassesByPathView(classesOfYear);
        mandatoryClasses = getMandatoryClasses(classesOfYear);

        List<List<Integer>> pathCombinations = getPathCombinations();

        List<AnnualClassCombination> allAnnualCombinations = new ArrayList<>();
        for (List<Integer> pathCombination : pathCombinations) {
            Set<String> mandatoryPathClasses = getClassesForPathCombination(pathCombination);
            mandatoryPathClasses.addAll(mandatoryClasses);

            Set<String> optionalClassesForThesePaths = getOptionalClassesForPaths(pathCombination);

            allAnnualCombinations.addAll(generateAnnualCombinations(mandatoryPathClasses, optionalClassesForThesePaths));
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
     */
    private Set<Integer> getAvailablePaths(List<DegreeClass> allClasses) {

        Set<Integer> paths = new TreeSet<>();
        // Atenção a esta instrução, isto vai aos arrays de todas as cadeiras do ano para inserir multiplas vezes
        // os mesmos paths, como é um SET não haverá repetidos, mas mesmo assim vai tudo lá para dentro
        for (DegreeClass degreeClass : allClasses) {
            if (degreeClass.hasPaths()) {
                for (int i = 0; i < degreeClass.getPaths().length; i++) {
                    paths.add(degreeClass.getPaths()[i]);
                }
            }
        }
        return paths;
    }


    /**
     * Creates a data structure that maps each path to the Set of possible classes that are mandatory for that
     * same path.
     * Useful as a means to fast access the classes for a path.
     */
    private Map<Integer, Set<String>> getClassesByPathView(List<DegreeClass> classesOfYear) {

        Map<Integer, Set<String>> pathDictionary = new TreeMap<>();

        for (Integer path : availablePaths) {
            pathDictionary.put(path, getPathMandatoryClasses(path, classesOfYear));
        }

        return pathDictionary;
    }

    /**
     * Returns a Set with the mandatory classes for a given path.
     */
    private Set<String> getPathMandatoryClasses(int path, List<DegreeClass> classesOfYear) {

        Set<String> classesOfPath = new TreeSet<>();

        for (DegreeClass degreeClass : classesOfYear) {

            if (degreeClass.hasPaths() && degreeClass.isPathMandatory(path)) {
                classesOfPath.add(degreeClass.getId());
            }
        }
        return classesOfPath;
    }


    private Map<Integer, Set<String>> getOptionalClassesByPathView(List<DegreeClass> classesOfYear) {

        Map<Integer, Set<String>> pathDictionary = new TreeMap<>();

        for (Integer path : availablePaths) {
            pathDictionary.put(path, getPathOptionalClasses(path, classesOfYear));
        }

        return pathDictionary;
    }

    /**
     * Returns a Set with the mandatory classes for a given path.
     */
    private Set<String> getPathOptionalClasses(int path, List<DegreeClass> classesOfYear) {

        Set<String> optionalClassesForPath = new TreeSet<>();

        for (DegreeClass degreeClass : classesOfYear) {
            if (degreeClass.hasPaths() && degreeClass.isOptionalForPath(path)) {
                optionalClassesForPath.add(degreeClass.getId());
            }
            if (!degreeClass.hasPaths() && degreeClass.isOptionalClass()) {
                optionalClassesForPath.add(degreeClass.getId());
            }
        }
        return optionalClassesForPath;
    }

    /**
     * Pretty self-explanatory, returns a collection with all the mandatory classes for this given year.
     */
    private Collection<String> getMandatoryClasses(List<DegreeClass> classesOfYear) {

        Set<String> mandatoryClasses = new TreeSet<>();

        for (DegreeClass degreeClass : classesOfYear) {
            if (degreeClass.isMandatoryClass()) {
                mandatoryClasses.add(degreeClass.getId());
            }
        }
        return mandatoryClasses;
    }

    /**
     * Returns a List with all the possible combinations between Paths.
     * This is way more useful than calculating the combinations of classes individually since a
     * Path is no more than a collection of classes.
     */
    private List<List<Integer>> getPathCombinations() {

        ICombinatoricsVector<Integer> optionalVector = Factory.createVector(availablePaths.toArray(new Integer[availablePaths.size()]));
        // creates the generator that can calculate the needed combinations.
        Generator<Integer> optionalCombinations = Factory.createSimpleCombinationGenerator(optionalVector, 2);

        List<List<Integer>> resultingCombinations = new ArrayList<>();

        // Now iterate over all the possible combinations, add them the mandatory classes and add them to the final object.
        for (ICombinatoricsVector<Integer> comboVector : optionalCombinations.generateAllObjects()) {
            resultingCombinations.add(comboVector.getVector());
        }

        return resultingCombinations;
    }

    /**
     * This basically just joins the Sets of the classes of all the paths given as parameter.
     */
    private Set<String> getClassesForPathCombination(List<Integer> paths) {
        Set<String> mandatoryClasses = new TreeSet<>();

        for (Integer path : paths) {
            mandatoryClasses.addAll(mandatoryDegreeClassesByPath.get(path));
        }
        return mandatoryClasses;
    }

    private Set<String> getOptionalClassesForPaths(List<Integer> paths) {

        Set<String> possibleOptionalClasses = new TreeSet<>();

        // Get the first of the bunch to compare with all the others
        possibleOptionalClasses.addAll(optionalDegreeClassesByPath.get(paths.get(0)));

        for (int i = 1; i < paths.size(); i++) {
            possibleOptionalClasses = Sets.intersection(optionalDegreeClassesByPath.get(paths.get(i)), possibleOptionalClasses);
        }

        return possibleOptionalClasses;
    }


    /**
     * This is the function that does most of the work.
     * This will finally combine all the mandatory classes with combinations of the optional classes
     * which effectively totals the greater amount of annual combinations.
     */
    private List<AnnualClassCombination> generateAnnualCombinations(Set<String> mandatoryClasses, Set<String> optionalClasses) {

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
