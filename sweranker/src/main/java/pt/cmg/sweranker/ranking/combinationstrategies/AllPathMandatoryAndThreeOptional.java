package pt.cmg.sweranker.ranking.combinationstrategies;


import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.AnnualClassCombination;

public class AllPathMandatoryAndThreeOptional implements ClassCombinationStrategy {


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

        Map<Integer, List<String>> baseCombinationPerPath;

        for (Integer path : availablePaths) {

            List<String> mandatoryClasses = getMandatoryPathClasses(path, allDegreeClasses);
            List<String> optionalClasses = getOptionalPathClasses(path, allDegreeClasses);

            allAnnualCombinations.addAll(generateAnnualCombinations(mandatoryClasses, optionalClasses));

        }

        return allAnnualCombinations;
    }


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

    private List<String> getOptionalPathClasses(int path, List<DegreeClass> allClasses) {

        List<String> optionalClasses = new ArrayList<>();
        for (DegreeClass degreeClass : allClasses) {
            if (degreeClass.isOptionalClass() && !degreeClass.isPathMandatory(path)) {
                optionalClasses.add(degreeClass.getId());
            }

        }
        return optionalClasses;
    }


    private List<AnnualClassCombination> generateAnnualCombinations(List<String> mandatoryClasses, List<String> optionalClasses) {

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
