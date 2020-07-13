package mindcet.natureg.MainMenu_Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mindcet.natureg.Login.LoginActivity;
import mindcet.natureg.R;
import mindcet.natureg.Utilities.HideSysteUI;
import mindcet.natureg.Utilities.Question;

public class Trivia_Fragment extends Fragment {

    private  List allAnimals;
    private int questionsPerAnimal = 5;
    private int getWrongIndex = 4;
    private Map  animals = new HashMap();
    private static int userScore = 0;
    private Button answer1;
    private Button answer2;
    private Button answer3;
    private Button answer4;
    private TextView currentScoreDisplayed;
    private TextView currentQuestionDisplayed;
    private Question currentQuestion= null;
    private String status;
    {
        try {
            userScore = Integer.valueOf(LoginActivity.getUserDetails().get("Score").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        try {
            status = LoginActivity.getUserDetails().get("Status").toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }


        View RootView = inflater.inflate(R.layout.fragment_trivia, container, false);
        answer1 = RootView.findViewById(R.id.btn_one);
        answer2 = RootView.findViewById(R.id.btn_two);
        answer3 = RootView.findViewById(R.id.btn_three);
        answer4 = RootView.findViewById(R.id.btn_four);
        currentQuestionDisplayed = RootView.findViewById(R.id.tv_question);
        currentScoreDisplayed = RootView.findViewById(R.id.tv_score);
        currentScoreDisplayed.setText("Score:"+String.valueOf(userScore));
        initTrivia();
        loadQuestion();


        answer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(answer1.getText().toString());
                loadQuestion();

            }
        });

        answer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(answer2.getText().toString());
                loadQuestion();
            }
        });

        answer3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(answer3.getText().toString());
                loadQuestion();
            }
        });

        answer4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(answer4.getText().toString());
                loadQuestion();
            }
        });



        return RootView;
    }

    private void checkAnswer(String answer) {
        if (answer.equals(currentQuestion.getCorrectAnswer())){
            showCurrectAnswerDialog("Correct", "The answer is: " + currentQuestion.getCorrectAnswer());
            userScore += 10;
            currentScoreDisplayed.setText("Score:"+String.valueOf(userScore));
        }
        else{
            showCurrectAnswerDialog("Wrong","The Answer is: "+currentQuestion.getCorrectAnswer());
        }

    }

    public static  int getScore(){
        return userScore;
    }


    private void loadQuestion() {
        int randAnswer;
        Random rand = new Random();
        if (animals.size() == 0) {
            initTrivia();
        }

        int animalRand = rand.nextInt(animals.size());
        ArrayList<Question> currentList = (ArrayList<Question>) animals.get(allAnimals.get(animalRand));
        int questionRand = rand.nextInt(currentList.size());
        currentQuestion = currentList.get(questionRand);
        currentQuestionDisplayed.setText(currentQuestion.getQuestion());
        ArrayList<String> answerList = new ArrayList<String>() {
            {
                add(currentQuestion.getAnswerOne()); add(currentQuestion.getAnswerTwo());
                add(currentQuestion.getAnswerThree()); add(currentQuestion.getAnswerFour());
            }

        };
        randAnswer = rand.nextInt(answerList.size());
        answer1.setText(answerList.get(randAnswer));
        answerList.remove(randAnswer);
        randAnswer = rand.nextInt(answerList.size());
        answer2.setText(answerList.get(randAnswer));
        answerList.remove(randAnswer);
        randAnswer = rand.nextInt(answerList.size());
        answer3.setText(answerList.get(randAnswer));
        answerList.remove(randAnswer);
        answer4.setText(answerList.get(0));
        currentList.remove(questionRand);
        if(currentList.size() == 0){
            animals.remove(allAnimals.get(animalRand));
            allAnimals.remove(animalRand);
        }

    }



    private void initTrivia() {

            allAnimals = new ArrayList<String>() {
                {
                    add("gecko");
                    add("hoopoe");
                    add("honeyBadger");
                    add("stellagama");
                    add("frog");
                    add("ciconia");
                    add("otter");
                    add("mongoose");
                    add("fruitBat");

                }
            };

        for(int i = 0; i < allAnimals.size() ; i++){
            ArrayList<Question> specificAnimalQuestions = new ArrayList<>();
            for(int j = 1 ; j <=questionsPerAnimal ; j++){
                List questionList = new ArrayList<>();
                int resId = getResources().getIdentifier(allAnimals.get(i)+"_question_"+j, "string",getActivity().getPackageName());
                questionList.add(getString(resId));
                for(int k = 0 ; k < getWrongIndex; k++){
                    int wrongAnswerID = getResources().getIdentifier(allAnimals.get(i)+"_question_"+j+"_wrong_"+k, "string",getActivity().getPackageName());
                    questionList.add(getString(wrongAnswerID));
                }
                Question question = new Question(questionList.get(0).toString(),questionList.get(1).toString(),
                        questionList.get(2).toString(),questionList.get(3).toString(),questionList.get(4).toString());
                specificAnimalQuestions.add(question);
            }
            animals.put(allAnimals.get(i),specificAnimalQuestions);
        }
    }


    private void showCurrectAnswerDialog(String title,String msg){
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton(R.string.confirm,  new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        HideSysteUI.hideSystemUI(getActivity());
                    }
                })
                .show();


    }



}
