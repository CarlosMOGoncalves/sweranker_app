package pt.cmg.sweranker.ranking;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * This class represents a single score for a complete Degree program or part of it (a Degree Class
 * or an Annual Combination of classes).
 * <p>
 * It is not overly complicated. It basically has a counter for the number of time a given Degree
 * Class Topic has appeared, a counter for the number of times any topic for a given KA has appeared
 * (meaning that if Topic 1 and Topic 3 are both related to KA 1 then the KA will have the number of times
 * the Topic 1 has appeared plus the number of time Topic 3 has appeared) and an average percentage
 * calculation for the KAs in the whole of the calculation.
 * <p>
 * More info on the class comments.
 * <p>
 * Created by Carlos on 07/03/2017.
 */

public class SweScore extends RealmObject {

    public static final String TYPE_CLASS_SCORE = "C";
    public static final String TYPE_ANNUAL_SCORE = "A";
    public static final String TYPE_DEGREE_SCORE = "D";
    public static final String TYPE_ON_DEMAND = "O";


    /**
     * Very important -> this id is:
     * the Degree Class Id if this is a Degree Class Score,
     * the Annual Combination Id if this is an Annual Combination Score or
     * the Degree Combination Id if this is a Degree Combination Score.
     */
    @PrimaryKey
    private String id;

    /**
     * Either TYPE_CLASS_SCORE, TYPE_ANNUAL_SCORE or TYPE_DEGREE_SCORE.
     * It should be an Enum if Realm supported it.
     * Update: actually a char will be just fine because of memory concerns.
     */
    private String scoreType;

    /**
     * A lot of attention here, this is a byte to save space and because this ID will NEVER
     * be higher than 127 (which the max range for a positive byte).
     * If I ever have more than 120 degrees this needs to be changed... but realistically if I ever
     * get over 10 this will have to be converted to a web app anyway.
     */
    private byte degreeId;

    // These are for storing the values in Realm
    private short topicCounter1;
    private short topicCounter2;
    private short topicCounter3;
    private short topicCounter4;
    private short topicCounter5;
    private short topicCounter6;
    private short topicCounter7;
    private short topicCounter8;
    private short topicCounter9;
    private short topicCounter10;
    private short topicCounter11;
    private short topicCounter12;
    private short topicCounter13;
    private short topicCounter14;
    private short topicCounter15;
    private short topicCounter16;
    private short topicCounter17;
    private short topicCounter18;
    private short topicCounter19;
    private short topicCounter20;
    private short topicCounter21;
    private short topicCounter22;
    private short topicCounter23;
    private short topicCounter24;
    private short topicCounter25;
    private short topicCounter26;
    private short topicCounter27;
    private short topicCounter28;
    private short topicCounter29;
    private short topicCounter30;
    private short topicCounter31;
    private short topicCounter32;
    private short topicCounter33;
    private short topicCounter34;
    private short topicCounter35;
    private short topicCounter36;
    private short topicCounter37;
    private short topicCounter38;
    private short topicCounter39;
    private short topicCounter40;
    private short topicCounter41;
    private short topicCounter42;
    private short topicCounter43;
    private short topicCounter44;
    private short topicCounter45;
    private short topicCounter46;
    private short topicCounter47;
    private short topicCounter48;
    private short topicCounter49;
    private short topicCounter50;
    private short topicCounter51;
    private short topicCounter52;
    private short topicCounter53;
    private short topicCounter54;
    private short topicCounter55;
    private short topicCounter56;
    private short topicCounter57;
    private short topicCounter58;
    private short topicCounter59;
    private short topicCounter60;
    private short topicCounter61;
    private short topicCounter62;
    private short topicCounter63;
    private short topicCounter64;
    private short topicCounter65;
    private short topicCounter66;
    private short topicCounter67;
    private short topicCounter68;
    private short topicCounter69;
    private short topicCounter70;
    private short topicCounter71;
    private short topicCounter72;
    private short topicCounter73;
    private short topicCounter74;
    private short topicCounter75;
    private short topicCounter76;
    private short topicCounter77;
    private short topicCounter78;
    private short topicCounter79;
    private short topicCounter80;
    private short topicCounter81;
    private short topicCounter82;
    private short topicCounter83;
    private short topicCounter84;
    private short topicCounter85;
    private short topicCounter86;
    private short topicCounter87;
    private short topicCounter88;
    private short topicCounter89;
    private short topicCounter90;
    private short topicCounter91;
    private short topicCounter92;
    private short topicCounter93;
    private short topicCounter94;
    private short topicCounter95;
    private short topicCounter96;
    private short topicCounter97;
    private short topicCounter98;
    private short topicCounter99;
    private short topicCounter100;
    private short topicCounter101;
    private short topicCounter102;

    @Ignore
    private short[] topicCounters;

    private short kaCounter1;
    private short kaCounter2;
    private short kaCounter3;
    private short kaCounter4;
    private short kaCounter5;
    private short kaCounter6;
    private short kaCounter7;
    private short kaCounter8;
    private short kaCounter9;
    private short kaCounter10;
    private short kaCounter11;
    private short kaCounter12;
    private short kaCounter13;
    private short kaCounter14;
    private short kaCounter15;
    private short kaCounter16;

    @Ignore
    private short[] kaCounters;


    private float kaPercent1;
    private float kaPercent2;
    private float kaPercent3;
    private float kaPercent4;
    private float kaPercent5;
    private float kaPercent6;
    private float kaPercent7;
    private float kaPercent8;
    private float kaPercent9;
    private float kaPercent10;
    private float kaPercent11;
    private float kaPercent12;
    private float kaPercent13;
    private float kaPercent14;
    private float kaPercent15;
    private float kaPercent16;

    @Ignore
    private float[] kaPercents;


    // Used to calculate the percents, SHORT will do just fine
    private short totalTopicCount;

    public SweScore() {
        topicCounters = null;
        kaCounters = null;
        kaPercents = null;
    }

    public SweScore(String scoreType) {
        this.scoreType = scoreType;
        degreeId = 0;
        topicCounters = null;
        kaCounters = null;
        kaPercents = null;
    }

    public SweScore(String id, byte degreeId, String scoreType) {
        this.id = id;
        this.scoreType = scoreType;
        this.degreeId = degreeId;
        topicCounters = null;
        kaCounters = null;
        kaPercents = null;
    }

    public SweScore(SweScore anotherScore) {
        id = anotherScore.getId();
        scoreType = anotherScore.getScoreType();
        degreeId = anotherScore.getDegreeId();
        setKaCounters(anotherScore.getKaCounters());
        setTopicCounters(anotherScore.getTopicCounters());
        totalTopicCount = anotherScore.getTotalTopicCount();
        setKaPercents(anotherScore.getKaPercents());
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }

    public short getTotalTopicCount() {
        return totalTopicCount;
    }

    public void setTotalTopicCount(short topicCount) {
        totalTopicCount = topicCount;
    }

    public short[] getTopicCounters() {
        if (topicCounters == null) {
            topicCounters = new short[102];
            topicCounters[0] = topicCounter1;
            topicCounters[1] = topicCounter2;
            topicCounters[2] = topicCounter3;
            topicCounters[3] = topicCounter4;
            topicCounters[4] = topicCounter5;
            topicCounters[5] = topicCounter6;
            topicCounters[6] = topicCounter7;
            topicCounters[7] = topicCounter8;
            topicCounters[8] = topicCounter9;
            topicCounters[9] = topicCounter10;
            topicCounters[10] = topicCounter11;
            topicCounters[11] = topicCounter12;
            topicCounters[12] = topicCounter13;
            topicCounters[13] = topicCounter14;
            topicCounters[14] = topicCounter15;
            topicCounters[15] = topicCounter16;
            topicCounters[16] = topicCounter17;
            topicCounters[17] = topicCounter18;
            topicCounters[18] = topicCounter19;
            topicCounters[19] = topicCounter20;
            topicCounters[20] = topicCounter21;
            topicCounters[21] = topicCounter22;
            topicCounters[22] = topicCounter23;
            topicCounters[23] = topicCounter24;
            topicCounters[24] = topicCounter25;
            topicCounters[25] = topicCounter26;
            topicCounters[26] = topicCounter27;
            topicCounters[27] = topicCounter28;
            topicCounters[28] = topicCounter29;
            topicCounters[29] = topicCounter30;
            topicCounters[30] = topicCounter31;
            topicCounters[31] = topicCounter32;
            topicCounters[32] = topicCounter33;
            topicCounters[33] = topicCounter34;
            topicCounters[34] = topicCounter35;
            topicCounters[35] = topicCounter36;
            topicCounters[36] = topicCounter37;
            topicCounters[37] = topicCounter38;
            topicCounters[38] = topicCounter39;
            topicCounters[39] = topicCounter40;
            topicCounters[40] = topicCounter41;
            topicCounters[41] = topicCounter42;
            topicCounters[42] = topicCounter43;
            topicCounters[43] = topicCounter44;
            topicCounters[44] = topicCounter45;
            topicCounters[45] = topicCounter46;
            topicCounters[46] = topicCounter47;
            topicCounters[47] = topicCounter48;
            topicCounters[48] = topicCounter49;
            topicCounters[49] = topicCounter50;
            topicCounters[50] = topicCounter51;
            topicCounters[51] = topicCounter52;
            topicCounters[52] = topicCounter53;
            topicCounters[53] = topicCounter54;
            topicCounters[54] = topicCounter55;
            topicCounters[55] = topicCounter56;
            topicCounters[56] = topicCounter57;
            topicCounters[57] = topicCounter58;
            topicCounters[58] = topicCounter59;
            topicCounters[59] = topicCounter60;
            topicCounters[60] = topicCounter61;
            topicCounters[61] = topicCounter62;
            topicCounters[62] = topicCounter63;
            topicCounters[63] = topicCounter64;
            topicCounters[64] = topicCounter65;
            topicCounters[65] = topicCounter66;
            topicCounters[66] = topicCounter67;
            topicCounters[67] = topicCounter68;
            topicCounters[68] = topicCounter69;
            topicCounters[69] = topicCounter70;
            topicCounters[70] = topicCounter71;
            topicCounters[71] = topicCounter72;
            topicCounters[72] = topicCounter73;
            topicCounters[73] = topicCounter74;
            topicCounters[74] = topicCounter75;
            topicCounters[75] = topicCounter76;
            topicCounters[76] = topicCounter77;
            topicCounters[77] = topicCounter78;
            topicCounters[78] = topicCounter79;
            topicCounters[79] = topicCounter80;
            topicCounters[80] = topicCounter81;
            topicCounters[81] = topicCounter82;
            topicCounters[82] = topicCounter83;
            topicCounters[83] = topicCounter84;
            topicCounters[84] = topicCounter85;
            topicCounters[85] = topicCounter86;
            topicCounters[86] = topicCounter87;
            topicCounters[87] = topicCounter88;
            topicCounters[88] = topicCounter89;
            topicCounters[89] = topicCounter90;
            topicCounters[90] = topicCounter91;
            topicCounters[91] = topicCounter92;
            topicCounters[92] = topicCounter93;
            topicCounters[93] = topicCounter94;
            topicCounters[94] = topicCounter95;
            topicCounters[95] = topicCounter96;
            topicCounters[96] = topicCounter97;
            topicCounters[97] = topicCounter98;
            topicCounters[98] = topicCounter99;
            topicCounters[99] = topicCounter100;
            topicCounters[100] = topicCounter101;
            topicCounters[101] = topicCounter102;
        }

        return topicCounters;
    }


    public short[] getKaCounters() {
        if (kaCounters == null) {
            kaCounters = new short[16];
            kaCounters[0] = kaCounter1;
            kaCounters[1] = kaCounter2;
            kaCounters[2] = kaCounter3;
            kaCounters[3] = kaCounter4;
            kaCounters[4] = kaCounter5;
            kaCounters[5] = kaCounter6;
            kaCounters[6] = kaCounter7;
            kaCounters[7] = kaCounter8;
            kaCounters[8] = kaCounter9;
            kaCounters[9] = kaCounter10;
            kaCounters[10] = kaCounter11;
            kaCounters[11] = kaCounter12;
            kaCounters[12] = kaCounter13;
            kaCounters[13] = kaCounter14;
            kaCounters[14] = kaCounter15;
            kaCounters[15] = kaCounter16;
        }

        return kaCounters;
    }

    public float[] getKaPercents() {
        if (kaPercents == null) {
            kaPercents = new float[16];
            kaPercents[0] = kaPercent1;
            kaPercents[1] = kaPercent2;
            kaPercents[2] = kaPercent3;
            kaPercents[3] = kaPercent4;
            kaPercents[4] = kaPercent5;
            kaPercents[5] = kaPercent6;
            kaPercents[6] = kaPercent7;
            kaPercents[7] = kaPercent8;
            kaPercents[8] = kaPercent9;
            kaPercents[9] = kaPercent10;
            kaPercents[10] = kaPercent11;
            kaPercents[11] = kaPercent12;
            kaPercents[12] = kaPercent13;
            kaPercents[13] = kaPercent14;
            kaPercents[14] = kaPercent15;
            kaPercents[15] = kaPercent16;
        }

        return kaPercents;
    }


    public void setTopicCounters(short... topicCounters) {
        topicCounter1 = topicCounters[0];
        topicCounter2 = topicCounters[1];
        topicCounter3 = topicCounters[2];
        topicCounter4 = topicCounters[3];
        topicCounter5 = topicCounters[4];
        topicCounter6 = topicCounters[5];
        topicCounter7 = topicCounters[6];
        topicCounter8 = topicCounters[7];
        topicCounter9 = topicCounters[8];
        topicCounter10 = topicCounters[9];
        topicCounter11 = topicCounters[10];
        topicCounter12 = topicCounters[11];
        topicCounter13 = topicCounters[12];
        topicCounter14 = topicCounters[13];
        topicCounter15 = topicCounters[14];
        topicCounter16 = topicCounters[15];
        topicCounter17 = topicCounters[16];
        topicCounter18 = topicCounters[17];
        topicCounter19 = topicCounters[18];
        topicCounter20 = topicCounters[19];
        topicCounter21 = topicCounters[20];
        topicCounter22 = topicCounters[21];
        topicCounter23 = topicCounters[22];
        topicCounter24 = topicCounters[23];
        topicCounter25 = topicCounters[24];
        topicCounter26 = topicCounters[25];
        topicCounter27 = topicCounters[26];
        topicCounter28 = topicCounters[27];
        topicCounter29 = topicCounters[28];
        topicCounter30 = topicCounters[29];
        topicCounter31 = topicCounters[30];
        topicCounter32 = topicCounters[31];
        topicCounter33 = topicCounters[32];
        topicCounter34 = topicCounters[33];
        topicCounter35 = topicCounters[34];
        topicCounter36 = topicCounters[35];
        topicCounter37 = topicCounters[36];
        topicCounter38 = topicCounters[37];
        topicCounter39 = topicCounters[38];
        topicCounter40 = topicCounters[39];
        topicCounter41 = topicCounters[40];
        topicCounter42 = topicCounters[41];
        topicCounter43 = topicCounters[42];
        topicCounter44 = topicCounters[43];
        topicCounter45 = topicCounters[44];
        topicCounter46 = topicCounters[45];
        topicCounter47 = topicCounters[46];
        topicCounter48 = topicCounters[47];
        topicCounter49 = topicCounters[48];
        topicCounter50 = topicCounters[49];
        topicCounter51 = topicCounters[50];
        topicCounter52 = topicCounters[51];
        topicCounter53 = topicCounters[52];
        topicCounter54 = topicCounters[53];
        topicCounter55 = topicCounters[54];
        topicCounter56 = topicCounters[55];
        topicCounter57 = topicCounters[56];
        topicCounter58 = topicCounters[57];
        topicCounter59 = topicCounters[58];
        topicCounter60 = topicCounters[59];
        topicCounter61 = topicCounters[60];
        topicCounter62 = topicCounters[61];
        topicCounter63 = topicCounters[62];
        topicCounter64 = topicCounters[63];
        topicCounter65 = topicCounters[64];
        topicCounter66 = topicCounters[65];
        topicCounter67 = topicCounters[66];
        topicCounter68 = topicCounters[67];
        topicCounter69 = topicCounters[68];
        topicCounter70 = topicCounters[69];
        topicCounter71 = topicCounters[70];
        topicCounter72 = topicCounters[71];
        topicCounter73 = topicCounters[72];
        topicCounter74 = topicCounters[73];
        topicCounter75 = topicCounters[74];
        topicCounter76 = topicCounters[75];
        topicCounter77 = topicCounters[76];
        topicCounter78 = topicCounters[77];
        topicCounter79 = topicCounters[78];
        topicCounter80 = topicCounters[79];
        topicCounter81 = topicCounters[80];
        topicCounter82 = topicCounters[81];
        topicCounter83 = topicCounters[82];
        topicCounter84 = topicCounters[83];
        topicCounter85 = topicCounters[84];
        topicCounter86 = topicCounters[85];
        topicCounter87 = topicCounters[86];
        topicCounter88 = topicCounters[87];
        topicCounter89 = topicCounters[88];
        topicCounter90 = topicCounters[89];
        topicCounter91 = topicCounters[90];
        topicCounter92 = topicCounters[91];
        topicCounter93 = topicCounters[92];
        topicCounter94 = topicCounters[93];
        topicCounter95 = topicCounters[94];
        topicCounter96 = topicCounters[95];
        topicCounter97 = topicCounters[96];
        topicCounter98 = topicCounters[97];
        topicCounter99 = topicCounters[98];
        topicCounter100 = topicCounters[99];
        topicCounter101 = topicCounters[100];
        topicCounter102 = topicCounters[101];
    }

    public void setKaCounters(short... kaCounters) {
        kaCounter1 = kaCounters[0];
        kaCounter2 = kaCounters[1];
        kaCounter3 = kaCounters[2];
        kaCounter4 = kaCounters[3];
        kaCounter5 = kaCounters[4];
        kaCounter6 = kaCounters[5];
        kaCounter7 = kaCounters[6];
        kaCounter8 = kaCounters[7];
        kaCounter9 = kaCounters[8];
        kaCounter10 = kaCounters[9];
        kaCounter11 = kaCounters[10];
        kaCounter12 = kaCounters[11];
        kaCounter13 = kaCounters[12];
        kaCounter14 = kaCounters[13];
        kaCounter15 = kaCounters[14];
        kaCounter16 = kaCounters[15];
    }


    public void setKaPercents(float... kaPercents) {
        kaPercent1 = kaPercents[0];
        kaPercent2 = kaPercents[1];
        kaPercent3 = kaPercents[2];
        kaPercent4 = kaPercents[3];
        kaPercent5 = kaPercents[4];
        kaPercent6 = kaPercents[5];
        kaPercent7 = kaPercents[6];
        kaPercent8 = kaPercents[7];
        kaPercent9 = kaPercents[8];
        kaPercent10 = kaPercents[9];
        kaPercent11 = kaPercents[10];
        kaPercent12 = kaPercents[11];
        kaPercent13 = kaPercents[12];
        kaPercent14 = kaPercents[13];
        kaPercent15 = kaPercents[14];
        kaPercent16 = kaPercents[15];
    }


    // Note that this works because an implicit conversion to an int is OK from a byte
    public byte getDegreeId() {
        return degreeId;
    }

    public void setDegreeId(int degreeId) {
        this.degreeId = (byte) degreeId;
    }

    public short getKaCounter(int kaId) {
        switch (kaId) {
            case 1:
                return kaCounter1;
            case 2:
                return kaCounter2;
            case 3:
                return kaCounter3;
            case 4:
                return kaCounter4;
            case 5:
                return kaCounter5;
            case 6:
                return kaCounter6;
            case 7:
                return kaCounter7;
            case 8:
                return kaCounter8;
            case 9:
                return kaCounter9;
            case 10:
                return kaCounter10;
            case 11:
                return kaCounter11;
            case 12:
                return kaCounter12;
            case 13:
                return kaCounter13;
            case 14:
                return kaCounter14;
            case 15:
                return kaCounter15;
            case 16:
                return kaCounter16;
            default:
                return kaCounter16;
        }
    }

    public short getKaTopicCounter(int kaTopicId) {
        switch (kaTopicId) {
            case 1:
                return topicCounter1;
            case 2:
                return topicCounter2;
            case 3:
                return topicCounter3;
            case 4:
                return topicCounter4;
            case 5:
                return topicCounter5;
            case 6:
                return topicCounter6;
            case 7:
                return topicCounter7;
            case 8:
                return topicCounter8;
            case 9:
                return topicCounter9;
            case 10:
                return topicCounter10;
            case 11:
                return topicCounter11;
            case 12:
                return topicCounter12;
            case 13:
                return topicCounter13;
            case 14:
                return topicCounter14;
            case 15:
                return topicCounter15;
            case 16:
                return topicCounter16;
            case 17:
                return topicCounter17;
            case 18:
                return topicCounter18;
            case 19:
                return topicCounter19;
            case 20:
                return topicCounter20;
            case 21:
                return topicCounter21;
            case 22:
                return topicCounter22;
            case 23:
                return topicCounter23;
            case 24:
                return topicCounter24;
            case 25:
                return topicCounter25;
            case 26:
                return topicCounter26;
            case 27:
                return topicCounter27;
            case 28:
                return topicCounter28;
            case 29:
                return topicCounter29;
            case 30:
                return topicCounter30;
            case 31:
                return topicCounter31;
            case 32:
                return topicCounter32;
            case 33:
                return topicCounter33;
            case 34:
                return topicCounter34;
            case 35:
                return topicCounter35;
            case 36:
                return topicCounter36;
            case 37:
                return topicCounter37;
            case 38:
                return topicCounter38;
            case 39:
                return topicCounter39;
            case 40:
                return topicCounter40;
            case 41:
                return topicCounter41;
            case 42:
                return topicCounter42;
            case 43:
                return topicCounter43;
            case 44:
                return topicCounter44;
            case 45:
                return topicCounter45;
            case 46:
                return topicCounter46;
            case 47:
                return topicCounter47;
            case 48:
                return topicCounter48;
            case 49:
                return topicCounter49;
            case 50:
                return topicCounter50;
            case 51:
                return topicCounter51;
            case 52:
                return topicCounter52;
            case 53:
                return topicCounter53;
            case 54:
                return topicCounter54;
            case 55:
                return topicCounter55;
            case 56:
                return topicCounter56;
            case 57:
                return topicCounter57;
            case 58:
                return topicCounter58;
            case 59:
                return topicCounter59;
            case 60:
                return topicCounter60;
            case 61:
                return topicCounter61;
            case 62:
                return topicCounter62;
            case 63:
                return topicCounter63;
            case 64:
                return topicCounter64;
            case 65:
                return topicCounter65;
            case 66:
                return topicCounter66;
            case 67:
                return topicCounter67;
            case 68:
                return topicCounter68;
            case 69:
                return topicCounter69;
            case 70:
                return topicCounter70;
            case 71:
                return topicCounter71;
            case 72:
                return topicCounter72;
            case 73:
                return topicCounter73;
            case 74:
                return topicCounter74;
            case 75:
                return topicCounter75;
            case 76:
                return topicCounter76;
            case 77:
                return topicCounter77;
            case 78:
                return topicCounter78;
            case 79:
                return topicCounter79;
            case 80:
                return topicCounter80;
            case 81:
                return topicCounter81;
            case 82:
                return topicCounter82;
            case 83:
                return topicCounter83;
            case 84:
                return topicCounter84;
            case 85:
                return topicCounter85;
            case 86:
                return topicCounter86;
            case 87:
                return topicCounter87;
            case 88:
                return topicCounter88;
            case 89:
                return topicCounter89;
            case 90:
                return topicCounter90;
            case 91:
                return topicCounter91;
            case 92:
                return topicCounter92;
            case 93:
                return topicCounter93;
            case 94:
                return topicCounter94;
            case 95:
                return topicCounter95;
            case 96:
                return topicCounter96;
            case 97:
                return topicCounter97;
            case 98:
                return topicCounter98;
            case 99:
                return topicCounter99;
            case 100:
                return topicCounter100;
            case 101:
                return topicCounter101;
            case 102:
                return topicCounter102;
            default:
                return topicCounter102;
        }
    }

    public float getKaPercent(int kaId) {
        switch (kaId) {
            case 1:
                return kaPercent1;
            case 2:
                return kaPercent2;
            case 3:
                return kaPercent3;
            case 4:
                return kaPercent4;
            case 5:
                return kaPercent5;
            case 6:
                return kaPercent6;
            case 7:
                return kaPercent7;
            case 8:
                return kaPercent8;
            case 9:
                return kaPercent9;
            case 10:
                return kaPercent10;
            case 11:
                return kaPercent11;
            case 12:
                return kaPercent12;
            case 13:
                return kaPercent13;
            case 14:
                return kaPercent14;
            case 15:
                return kaPercent15;
            case 16:
                return kaPercent16;
            default:
                return kaPercent16;
        }
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Combo ID : " + id);
        s.append(";Type:" + scoreType);
        s.append(";Total topics: " + totalTopicCount);
        return s.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
