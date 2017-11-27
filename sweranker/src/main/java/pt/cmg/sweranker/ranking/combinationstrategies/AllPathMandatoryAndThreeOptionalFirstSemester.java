package pt.cmg.sweranker.ranking.combinationstrategies;

import com.google.common.collect.Sets;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.AnnualClassCombination;

/**
 * Created by Carlos on 24/11/2017.
 * <p>
 * This is a class combination strategy that was custom-developed to calculate the combinations for the
 * 4th year of IST - Lisboa degree.
 * <p>
 * The logic behind it makes it a somewhat complicated calculation.
 * However, it results in a thankfully low number of class combinations because most degree classes are
 * combined into a package of classes (a path).
 * <p>
 * This is how it works: <br/>
 * 1 - Gets the different number of paths in the year <br/>
 * 2 - Gets a data view of the classes that are mandatory for each path <br/>
 * 3 - Gets a special view of all the classes of the first semester, this will be needed to calculate
 * classes that should only be used in the 5th year but have to be part of the 4th year package. <br/>
 * 4 - It then will mix and match the mandatory classes for each path with the optional ones, always making
 * sure that NO classes are repeated (that is why SETs were used) <br/>
 * 5 - That will result in a List of different annual combinations unique for each path. <br/>
 * <p>
 * Important notes: <br/>
 * a) This is a complicated degree organisation. The main idea is that a student can choose any number of
 * classes from a roster of 60 possible classes (until a ECTS credit limit, of course). Then, depending on
 * the classes that he chose, the student could be awarded a specialisation on his diploma. This happens
 * if he chooses more than 4 classes of a single path. However, this approach is ruinous for a calculation,
 * because choosing 8 classes from a roster of 60 amounts to roughly 2 billion possible combination(!!!) <br/>
 * b) Alternatively, the student can choose any two paths (paths are really just a SET of already combined classes)
 * which is way easier, because now we are looking at a max of 105 combinations C(15,2). <br/>
 * c) However, in the 5th year there is also a need to choose an additional 3 classes out of the same roster
 * used here (the ones of the 4th year). These are all of the 1st Semester and of course, cannot have been taken yet.
 * To tackle this problem I decided to just go ahead and calculate them all in the 4th year (because it is the only
 * place where I have runtime access to all the classes already chosen, during the combination calculation).
 * The good part is that the combinations are ACCURATE. The worst part is that the classes chosen that belong to
 * the 5th year are shown in the 4th year. I guess the ends justify the means.
 * Also, due to the strategy used to calculate the paths the number of possible combinations also benefits
 * from that choice. Choosing TWO paths and 3 classes from the remaining optional ones amount to 105 x 2600
 * which is roughly 250000 in total, way better than randomly choosing classes, which would go to the billions.
 */

public class AllPathMandatoryAndThreeOptionalFirstSemester implements ClassCombinationStrategy {

    private static String combinationIdBase;
    private static int idCounter;
    private static int currentYear;

    private static Set<Integer> availablePaths;
    private static Map<Integer, Set<String>> degreeClassesByPath;
    private static Set<String> degreeClassesOfFirstSemester;


    @Override
    public List<AnnualClassCombination> getAnnualClassCombinations(int targetYear, List<DegreeClass> allDegreeClasses) {

        currentYear = targetYear;
        combinationIdBase = "d_" + allDegreeClasses.get(0).getDegreeId() + "_y_" + targetYear + "_c_";
        idCounter = 1;


        List<DegreeClass> classesOfYear = getDegreeClassesOfYear(allDegreeClasses);

        availablePaths = getAvailablePaths(classesOfYear);
        degreeClassesByPath = getClassesByPathView(classesOfYear);
        degreeClassesOfFirstSemester = getFirstSemesterClasses(classesOfYear);


        List<AnnualClassCombination> allAnnualCombinations = new ArrayList<>();

        List<List<Integer>> pathCombinations = getPathCombinations();
        for (List<Integer> pathCombination : pathCombinations) {
            Set<String> mandatoryPathClasses = getClassesForPathCombination(pathCombination);
            Set<String> optionalPathClasses = Sets.difference(degreeClassesOfFirstSemester, mandatoryPathClasses);

            allAnnualCombinations.addAll(generateAnnualCombinations(mandatoryPathClasses, optionalPathClasses));
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
     * Returns the Set with all the available paths for this year's classes.
     * Some degrees have different paths to complete it and this is an approach to it.
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

    /**
     * Returns a Set with all the classes that are taught in the first semester of this year.
     * Again, this will be used to calculate a difference of classes
     */
    private Set<String> getFirstSemesterClasses(List<DegreeClass> classesOfYear) {

        Set<String> classesOfPath = new TreeSet<>();

        for (DegreeClass degreeClass : classesOfYear) {

            if (degreeClass.getSemester() == 1) {
                classesOfPath.add(degreeClass.getId());
            }
        }
        return classesOfPath;
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
            mandatoryClasses.addAll(degreeClassesByPath.get(path));
        }
        return mandatoryClasses;
    }


    /**
     * This is the function that does most of the work.
     * This will finally combine all the mandatory classes with combinations of the optional classes
     * which effectively totals the greater amount of annual combinations.
     */
    private List<AnnualClassCombination> generateAnnualCombinations(Set<String> mandatoryClasses, Set<String> optionalClasses) {

        ICombinatoricsVector<String> optionalVector = Factory.createVector(optionalClasses.toArray(new String[optionalClasses.size()]));

        // creates the generator that can calculate the needed combinations.
        Generator<String> optionalCombinations = Factory.createSimpleCombinationGenerator(optionalVector, 3);


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