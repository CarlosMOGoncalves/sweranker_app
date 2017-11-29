package pt.cmg.sweranker.ranking.combinationstrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.AnnualClassCombination;

/**
 * Created by Carlos on 29/11/2017.
 */

public class AllMandatoryAndOneOptionalPerSemester implements ClassCombinationStrategy {


    private static String combinationIdBase;
    private static int idCounter;
    private static int currentYear;


    @Override
    public List<AnnualClassCombination> getAnnualClassCombinations(int targetYear, List<DegreeClass> allDegreeClasses) {

        currentYear = targetYear;
        combinationIdBase = "d_" + allDegreeClasses.get(0).getDegreeId() + "_y_" + targetYear + "_c_";
        idCounter = 1;

        List<DegreeClass> classesOfYear = getDegreeClassesOfYear(allDegreeClasses);

        Set<String> mandatoryClasses = getMandatoryClasses(classesOfYear);

        Set<String> firstSemesterOptionals = getOptionalClasses(1, classesOfYear);
        Set<String> secondSemesterOptionals = getOptionalClasses(2, classesOfYear);

        List<AnnualClassCombination> combinationList = generateCombinations(mandatoryClasses, firstSemesterOptionals, secondSemesterOptionals);

        return combinationList;
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
     * Returns a Set with all the mandatory classes for this year.
     */
    private Set<String> getMandatoryClasses(List<DegreeClass> classesOfYear) {
        Set<String> mandatoryClasses = new TreeSet<>();

        for (DegreeClass degreeClass : classesOfYear) {
            if (degreeClass.isMandatoryClass()) {
                mandatoryClasses.add(degreeClass.getId());
            }
        }
        return mandatoryClasses;
    }


    private Set<String> getOptionalClasses(int semester, List<DegreeClass> degreeClassesOfYear) {

        Set<String> optionalClasses = new TreeSet<>();

        for (DegreeClass degreeClass : degreeClassesOfYear) {
            if (degreeClass.getSemester() == semester && degreeClass.isOptionalClass()) {
                optionalClasses.add(degreeClass.getId());
            }
        }
        return optionalClasses;
    }

    private List<AnnualClassCombination> generateCombinations(Set<String> mandatoryClasses, Set<String> firstSemesterOptionals, Set<String> secondSemesterOptionals) {

        List<AnnualClassCombination> finalCombinationList = new ArrayList<>();

        // First there is a creation of an intermediate list with the mandatory classes and each of the optionals for the 1st semester
        List<AnnualClassCombination> combinationsForFirstSemester = new ArrayList<>();
        for (String degreeClass : firstSemesterOptionals) {

            AnnualClassCombination currentCombination = new AnnualClassCombination(combinationIdBase + idCounter);
            currentCombination.setYear(currentYear);

            currentCombination.addDegreeClass(degreeClass);
            currentCombination.addDegreeClasses(mandatoryClasses);

            combinationsForFirstSemester.add(currentCombination);

            // increment counter to create a new ID for this particular combo
            idCounter++;
        }

        // Then that same list is expanded with the second semester optionals, for each of the intermediate list's elements
        for (String degreeClass : secondSemesterOptionals) {

            for (AnnualClassCombination annualCombination : combinationsForFirstSemester) {

                AnnualClassCombination currentCombination = new AnnualClassCombination(combinationIdBase + idCounter);
                currentCombination.setYear(currentYear);

                currentCombination.addDegreeClass(degreeClass);
                currentCombination.addDegreeClasses(annualCombination.getDegreeClassIds());

                finalCombinationList.add(currentCombination);

                // increment counter to create a new ID for this particular combo
                idCounter++;
            }

        }
        return finalCombinationList;
    }
}
