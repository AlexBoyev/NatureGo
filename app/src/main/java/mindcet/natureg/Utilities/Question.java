package mindcet.natureg.Utilities;

public class Question {

    private String question = null;

    private String answerOne = null;
    private String answerTwo = null;
    private String answerThree = null;
    private String answerFour = null;
    private String correctAnswer = null;

    public Question(String question, String answerOne, String answerTwo,
                    String answerThree, String answerFour) {

        this.question = question;
        //Answer choices
        this.answerOne = answerOne;
        this.answerTwo = answerTwo;
        this.answerThree = answerThree;
        this.answerFour = answerFour;
        //Correct answer
        this.correctAnswer = answerOne;
    }

    public String getAnswerFour() {
        return answerFour;
    }

    public String getAnswerThree() {
        return answerThree;
    }

    public String getAnswerTwo() {
        return answerTwo;
    }

    public String getAnswerOne() {
        return answerOne;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getQuestion() {
        return question;
    }
}

