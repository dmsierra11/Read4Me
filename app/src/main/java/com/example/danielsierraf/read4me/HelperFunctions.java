package com.example.danielsierraf.read4me;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by danielsierraf on 7/9/15.
 */
public class HelperFunctions {

    private static final String TAG = "HelperFunctions";
    private final int COARSE = 1;
    private final int FINE = 2;

    private ArrayList<String> wordList_;

    public HelperFunctions(){

    }

    public Word spellCheck(String str, int method){
        Log.d(TAG, "string: "+str);
        int letterCount = 0, errorCount = 0, lNoiseCount = 0, digitCount = 0, result_ = COARSE;
        String withoutStrangeMarks = "", output = "";
        float score = 0;
        str = str.trim();
        Log.d(TAG, "Recorriendo palabra");
        for (int i = 0; i < str.length(); i++){
            //si es una letra
            if (Character.isLetter(str.charAt(i))){
                Log.d(TAG, "Es letra");
                //se van limpiando símbolos que pueden haber sido confundidos con l, L, i o I
                withoutStrangeMarks += str.charAt(i);
                letterCount++;
                if (str.charAt(i) == 'l' || str.charAt(i) == 'L' || str.charAt(i) == 'I')
                    lNoiseCount++;
            }  else if (Character.isDigit(str.charAt(i))) {
                Log.d(TAG, "Es digito");
                digitCount++;
                withoutStrangeMarks += str.charAt(i);
            } else if (str.charAt(i) == '|' || str.charAt(i) == '/' || str.charAt(i) == '\\') {
                //si es un símbolo como |, \ o \ y esta al lado de un dígito, se asume que es un uno
                Log.d(TAG, "Es |, / o \\");
                if ((Character.isDigit(str.charAt(i-1)))
                        || ((i < str.length() - 1) && Character.isDigit(str.charAt(i+1)))) {
                    Log.d(TAG, "Es |, / o \\ y tiene numeros al lado");
                    withoutStrangeMarks += '1';
                    //str[i] = '1';
                    str = str.substring(0, i-1) + '1' + str.substring(i+1, str.length()-1);
                    digitCount++;
                } else {
                    Log.d(TAG, "Es una l");
                    withoutStrangeMarks += 'l';
                    errorCount++;
                    letterCount++;
                }
            } else if (str.charAt(i) == '[') {
                Log.d(TAG, "Es una L");
                withoutStrangeMarks += 'L';
                errorCount++;
                letterCount++;
            } else if (str.charAt(i) == ']') {
                Log.d(TAG, "Es una I");
                withoutStrangeMarks += 'I';
                errorCount++;
                letterCount++;
            } else{
                Log.d(TAG, "Es basura");
                withoutStrangeMarks += "";
                //str = str.substring(0, i-1) + ' ' + str.substring(i+1, str.length()-1);
            }
        }

        if (digitCount > 0 && letterCount == 0) {
            if (digitCount <= 5)
                output = str + " ";
        } else if (letterCount < 2) {
            if (result_ == FINE)
                output = str + " ";
        } else if ((errorCount + lNoiseCount) * 2 > letterCount) {
            // do nothing
        } else if (letterCount < str.length() / 2) {
            // don't show up garbbige
        }

        Log.d(TAG, "filtered: "+withoutStrangeMarks);
        output += withoutStrangeMarks + " ";

        /*else {
            // if (method == 1)
            // {
            // 	const string command("echo " + withoutStrangeMarks +
            // 			" | aspell -a >> output");
            // 	int r = system(command.c_str());
            // 	fstream fin("output");
            // 	string result;
            // 	int count = 0;
            //
            // 	while (fin >> result)
            // 	{
            // 		if (count)
            // 		{
            // 			count ++;
            // 			if (count >= 5)
            // 			{
            // 				output	+= result + " ";
            // 			}
            // 			if (count == 10)
            // 			{
            // 				if ((output)[output.length()-2]==',')
            // 					((output)[output.length()-2]=' ');
            // 				break;
            // 			}
            // 		}
            // 		if (result[0] == '&')
            // 		{
            // 			count++;
            // 			output += "{";
            // 		}
            // 		else if (result[0] == '*')
            // 		{
            // 			output += " " + str;
            // 			break;
            // 		}
            // 	}
            // 	if (count)
            // 		output += "}";
            // 	r = system("rm output");
            // }

            // dictionary search
            if (method == 2) {
                ArrayList<Word> topk = new ArrayList<Word>();
                String nearestWord;
                topk = getTopkWords(withoutStrangeMarks, 3, topk);
                if (result_ == COARSE) {
                    String topWord = topk.get(0).getWord();
                    output = topk.get(0).getWord() + " ";

                    if (topWord.length() < 3) {
                        if (topk.get(0).getScore() == 0)
                            score++;
                        else
                            output = "";
                    } else if (topWord.length() < 6) {
                        if (topk.get(0).getScore() * 5 <= topWord.length())
                            score++;
                        else
                            output = "";
                    } else {
                        if (topk.get(0).getScore() == 0)
                            score = topWord.length() * 2;
                        else if (topk.get(0).getScore() <= topWord.length())
                            score = topWord.length();
                    }
                } else if (result_ == FINE) {
                    if (topk.get(0).getScore() == 0) {
                        output = topk.get(0).getWord() + " ";
                        score += topk.get(0).getWord().length() * 2;
                    } else {
                        output = "{" + withoutStrangeMarks + "->";
                        // pick top 3 results
                        for (int i = 0; i < 3; i++) {
                            output += topk.get(i).getWord() + ":" + topk.get(i).getScore() + " ";
                        }
                        output += "} ";
                    }
                }
            }
        }*/

        Word word = new Word(output, score);

        return word;
    }

    /*float DetectText::spellCheck(string& str, string& output, int method) {
        int letterCount = 0, errorCount = 0, lNoiseCount = 0, digitCount = 0;
        string withoutStrangeMarks;
        float score = 0;
        str = trim(str);
        for (size_t i = 0; i < str.length(); i++) {
            //si es una letra
            if (isupper(str[i]) || islower(str[i])) {
                //se van limpiando símbolos que pueden haber sido confundidos con l, L, i o I
                withoutStrangeMarks += str[i];
                letterCount++;
                if (str[i] == 'l' || str[i] == 'L' || str[i] == 'I')
                    lNoiseCount++;
            } else if (isdigit(str[i])) {
                digitCount++;
                //	withoutStrangeMarks += str[i];
            } else if (str[i] == '|' || str[i] == '/' || str[i] == '\\') {
                //si es un símbolo como |, \ o \\ y esta al lado de un dígito, se asume que es un uno
                if ((i && isdigit(str[i - 1]))
                        || ((i < str.length() - 1) && isdigit(str[i + 1]))) {
                    withoutStrangeMarks += '1';
                    str[i] = '1';
                    digitCount++;
                } else {
                    withoutStrangeMarks += 'l';
                    errorCount++;
                    letterCount++;
                }
            } else if (str[i] == '[') {
                withoutStrangeMarks += 'L';
                errorCount++;
                letterCount++;
            } else if (str[i] == ']') {
                withoutStrangeMarks += 'I';
                errorCount++;
                letterCount++;
            } else {
                str[i] = ' ';
            }
        }

        if (digitCount > 0 && letterCount == 0) {
            if (digitCount <= 5)
                output = str + " ";
        } else if (letterCount < 2) {
            if (result_ == FINE)
                output = str + " ";
        } else if ((errorCount + lNoiseCount) * 2 > letterCount) {
            // do nothing
        } else if (letterCount < static_cast<int>(str.length()) / 2) {
            // don't show up garbbige
        } else {
            // if (method == 1)
            // {
            // 	const string command("echo " + withoutStrangeMarks +
            // 			" | aspell -a >> output");
            // 	int r = system(command.c_str());
            // 	fstream fin("output");
            // 	string result;
            // 	int count = 0;
            //
            // 	while (fin >> result)
            // 	{
            // 		if (count)
            // 		{
            // 			count ++;
            // 			if (count >= 5)
            // 			{
            // 				output	+= result + " ";
            // 			}
            // 			if (count == 10)
            // 			{
            // 				if ((output)[output.length()-2]==',')
            // 					((output)[output.length()-2]=' ');
            // 				break;
            // 			}
            // 		}
            // 		if (result[0] == '&')
            // 		{
            // 			count++;
            // 			output += "{";
            // 		}
            // 		else if (result[0] == '*')
            // 		{
            // 			output += " " + str;
            // 			break;
            // 		}
            // 	}
            // 	if (count)
            // 		output += "}";
            // 	r = system("rm output");
            // }

            // dictionary search
            if (method == 2) {
                vector < Word > topk;
                string nearestWord;
                getTopkWords(withoutStrangeMarks, 3, topk);
                if (result_ == COARSE) {
                    string topWord = topk[0].word;
                    output = topk[0].word + " ";

                    if (topWord.length() < 3) {
                        if (topk[0].score == 0)
                            score++;
                        else
                            output = "";
                    } else if (topWord.length() < 6) {
                        if (topk[0].score * 5 <= topWord.length())
                            score++;
                        else
                            output = "";
                    } else {
                        if (topk[0].score == 0)
                            score = topWord.length() * 2;
                        else if (topk[0].score <= topWord.length())
                            score = topWord.length();
                    }
                } else if (result_ == FINE) {
                    if (topk[0].score == 0) {
                        output = topk[0].word + " ";
                        score += topk[0].word.length() * 2;
                    } else {
                        output = "{" + withoutStrangeMarks + "->";
                        // pick top 3 results
                        for (int i = 0; i < 3; i++) {
                            stringstream ss;
                            ss << topk[i].score;
                            string s = ss.str();
                            output = output + topk[i].word + ":" + s + " ";
                        }
                        output += "} ";
                    }
                }
            }
        }

        return score;
    }*/

    /*void DetectText::readLetterCorrelation(const char* file) {
        ifstream fin(file);
        correlation_ = Mat(62, 62, CV_32F, Scalar(0));
        float number;
        for (int i = 0; i < 62; i++)
            for (int j = 0; j < 62; j++) {
                assert(fin >> number);
                correlation_.at<float>(i, j) = number;
            }
    }*/

    /*void DetectText::readLetterCorrelation(int fd) {
        FILE* fp = fdopen(fd, "r");
        correlation_ = Mat(62, 62, CV_32F, Scalar(0));
        float number;
        for (int i = 0; i < 62; i++)
            for (int j = 0; j < 62; j++) {
            /*fscanf(fp, "%f", &number);
            __android_log_print(ANDROID_LOG_VERBOSE, "DetectText",
                                "Correlation floats : %lf", number);*/
                /*correlation_.at<float>(i, j) = number;
            }
    }*/

    /*void DetectText::readWordList(const char* filename) {
        ifstream fin(filename);
        string word;
        wordList_.clear();
        while (fin >> word) {
            wordList_.push_back(word);
        }
        assert(wordList_.size());
        cout << "read in " << wordList_.size() << " words from " << string(filename)
                << endl;
    }*/

    /*void DetectText::readWordList(int fd) {
        FILE* fp = fdopen(fd, "r");
        //	__android_log_print(ANDROID_LOG_VERBOSE, "DetectText", "Dictionary : %p", fp);
        char word[256];
        wordList_.clear();
        //while(fscanf(fp, "%s", word))
        for (int i = 0; i < 280354; i++) {
            //fscanf(fp, "%s", &word);
            cout << "Word: " << &word;
            //__android_log_print(ANDROID_LOG_VERBOSE, "DetectText", "Dictionary : %s", word);
            wordList_.push_back(string(word));
        }
        //	assert(wordList_.size());
        //	cout << "read in " <<  wordList_.size() << " words from "
        //			<< string(filename) << endl;
    }*/

    /*void DetectText::getNearestWord(const string& str, string& nearestWord) {
        cout << "start searching match for " << str << endl;
        float score, lowestScore = 100;
        int referenceScore;
        size_t index = 0;
        for (size_t i = 0; i < wordList_.size(); ++i) {
            cout << "matching...." << wordList_[i];
            score = editDistanceFont(str, wordList_[i]);
            referenceScore = editDistance(str, wordList_[i]);
            cout << " " << score << " " << referenceScore << endl;
            if (score < lowestScore) {
                lowestScore = score;
                cout << "AHA! better!" << endl;
                index = i;
            }
        }
        nearestWord = wordList_[index];
        cout << nearestWord << " got the lowest score: " << lowestScore << endl;
    }*/

    public ArrayList<Word> getTopkWords(String str, int k, ArrayList<Word> words){
        float score, lowestScore = 100;
        words.clear();

        /*for (int i = 0; i < wordList_.size(); i++) {
            score = editDistanceFont(str, wordList_.get(i));
            if (score < lowestScore) {
                Word w = Word(wordList_[i], score);
                lowestScore = insertToList(words, w);
            }
        }*/

        return words;
    }

    /*void DetectText::getTopkWords(const string& str, const int k,
                                  vector<Word>& words) {
        float score, lowestScore = 100;
        words.clear();
        words.resize(k);

        for (size_t i = 0; i < wordList_.size(); i++) {
            score = editDistanceFont(str, wordList_[i]);
            if (score < lowestScore) {
                Word w = Word(wordList_[i], score);
                lowestScore = insertToList(words, w);
            }
        }
    }*/

    // return lowest score in the list
    /*float DetectText::insertToList(vector<Word>& words, Word& word) {
        // first search for the position
        size_t index = 0;

        for (size_t i = 0; i < words.size(); i++) {
            index = i;
            if (word.score < words[i].score) {
                break;
            }
        }
        if (index != words.size()) {
            for (size_t i = words.size() - 1; i > index; i--) {
                words[i] = words[i - 1];
            }
            words[index] = word;
        }
        return words[words.size() - 1].score;
    }*/

    // use correlation as indicator of distance
    /*public float editDistanceFont(String s, String t) {
        double penalty = 0.7;

        int n = s.length();
        int m = t.length();

        if (n == 0)
            return m;
        if (m == 0)
            return n;

        float **d = new float*[n + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i] = new float[m + 1];
            memset(d[i], 0, (m + 1) * sizeof(float));
        }

        for (int i = 0; i < n + 1; i++)
            d[i][0] = i;
        for (int j = 0; j < m + 1; j++)
            d[0][j] = j;

        for (int i = 1; i < n + 1; i++) {
            char sc = s[i - 1];
            for (int j = 1; j < m + 1; j++) {
                float v = d[i - 1][j - 1];
                if ((t[j - 1] != sc)) {
                    float correlate = correlation_.at<float>(
                            getCorrelationIndex(t[j - 1]), getCorrelationIndex(sc));
                    v = v + 1 - correlate;
                }
                d[i][j] = min(min(d[i - 1][j] + penalty, d[i][j - 1] + penalty), v);
            }
        }
        float result = d[n][m];
        for (int i = 0; i < n + 1; i++)
            delete[] d[i];
        delete[] d;
        return result;
    }*/

    // get index in correlation matrix for given char
    /*int DetectText::getCorrelationIndex(char letter) {
        if (islower(letter)) {
            return letter - 'a';
        } else if (isupper(letter)) {
            return letter - 'A' + 26;
        } else if (isdigit(letter)) {
            return letter - '0' + 52;
        }
        cout << "illigal letter: " << letter << endl;
        assert(false);
        return -1;
    }*/

    // regular editDistance
    /*int DetectText::editDistance(const string& s, const string& t) {
        int n = s.length();
        int m = t.length();

        if (n == 0)
            return m;
        if (m == 0)
            return n;

        int **d = new int*[n + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i] = new int[m + 1];
            memset(d[i], 0, (m + 1) * sizeof(int));
        }

        for (int i = 0; i < n + 1; i++)
            d[i][0] = i;
        for (int j = 0; j < m + 1; j++)
            d[0][j] = j;

        for (int i = 1; i < n + 1; i++) {
            char sc = s[i - 1];
            for (int j = 1; j < m + 1; j++) {
                int v = d[i - 1][j - 1];
                if (t[j - 1] != sc)
                    v++;
                d[i][j] = min(min(d[i - 1][j] + 1, d[i][j - 1] + 1), v);
            }
        }
        return d[n][m];
    }*/
}
