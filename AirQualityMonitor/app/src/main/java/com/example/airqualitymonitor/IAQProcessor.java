package com.example.airqualitymonitor;

public class IAQProcessor
{
    public static int calculateIAQ(double humidity, double co2)
    {
        double humScore, gasScore;
        double gasReference = co2*1000.0;
        double humReference = 40;

        // Humidity contribution
        if (humidity >= 38 && humidity <=42)
        {
            humScore = 0.25 * 100;
        } else {
            if (humidity < 38)
            {
                humScore = 0.25/humReference * humidity * 100;
            } else
            {
                humScore = ((-0.25/(100-humReference)*humidity) + 0.416666) * 100;
            }
        }

        // Gas contribution
        double gasLowerLimit = 5000;
        double gasUpperLimit = 70000;

        if(gasReference > gasUpperLimit) gasReference = gasUpperLimit;
        if(gasReference < gasLowerLimit) gasReference = gasLowerLimit;

        gasScore = (0.75/(gasUpperLimit - gasLowerLimit)* gasReference -
                (gasLowerLimit*(0.75/(gasUpperLimit-gasLowerLimit))))*100;

        double airQualityScore = humScore + gasScore;

        double score = (100-airQualityScore)*5;

        if (score >= 301) return 9;
        else if (score >= 251 && score <= 300) return 25;
        else if (score >= 201 && score <= 250 ) return 40;
        else if (score >= 151 && score <= 200) return 57;
        else if (score >=  51 && score <= 150) return 75;
        else if (score <= 50) return 90;

        return 100;
    }
}
