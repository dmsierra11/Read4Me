/**
 * Implementation based on "Detecting Text in Natural Scenes with
 * Stroke Width Transform", Boris Epshtein, Eyal Ofek, Yonatan Wexler
 * CVPR 2010
 *
 */

/** \author Menglong Zhu */

#include "text_detect.h"
//#include <ros/ros.h>

#include <iostream>
#include <fstream>
#include <stack>
#include <vector>
#include <string>
#include <sstream>
#include <stdio.h>
//#include <tesseract/baseapi.h>
//#include <leptonica/allheaders.h>

//#include <android/log.h>

using namespace cv;
using namespace std;

DetectText::DetectText() :
maxStrokeWidth_(0), initialStrokeWidth_(0),
firstPass_(true), result_(COARSE),
nComponent_(0), maxLetterHeight_(0),
minLetterHeight_(0), textDisplayOffset_(1) {}

DetectText::~DetectText() {}

/* getters */
Mat&
DetectText::getDetection() {
    return detection_;
}

vector<string>&
DetectText::getWords() {
    return wordsBothSides_;
}

vector<Rect>&
DetectText::getBoundingBoxes(){
    return boundingBoxes_;
}

vector<Rect>&
DetectText::getBoxesWords(){
    return boxesBothSides_;
}
/* internal process */
void DetectText::detect() {
    
    Mat imGray(originalImage_.size(),CV_8UC1, Scalar(0));
    cvtColor(originalImage_, imGray, CV_RGB2GRAY);
    
    boundingBoxes_.clear();
    boxesBothSides_.clear();
    wordsBothSides_.clear();
    boxesScores_.clear();
    
    preprocess(imGray);
    firstPass_ = true;
    pipeline(1);
    firstPass_ = false;
    pipeline(-1);
    
    overlapBoundingBoxes(boundingBoxes_);
    showBoundingBoxes(boundingBoxes_);
    
}

void DetectText::detect(Mat& image) {
    originalImage_ = image.clone();
    mode_ = STREAM;
    
    Mat imGray(originalImage_.size(),CV_8UC1, Scalar(0));
    cvtColor(originalImage_, imGray, CV_RGB2GRAY);
    
    boundingBoxes_.clear();
    boxesBothSides_.clear();
    wordsBothSides_.clear();
    boxesScores_.clear();
    
    preprocess(imGray);
    firstPass_ = true;
    pipeline(1);
    firstPass_ = false;
    pipeline(-1);
    
    overlapBoundingBoxes(boundingBoxes_);
    //showBoundingBoxes(boundingBoxes_);
    
    /*Mat resultResized;
    resultResized.create(image.rows,image.cols, CV_8UC3);
    resize(detection_, resultResized, resultResized.size(), 0, 0, INTER_CUBIC);
    
    image = resultResized;*/
}

/*void DetectText::read(const char* lang){
    lang_ = lang;
    printf ("Reading native in %s \n", lang_ );
    vector<Mat> segments = segment(boundingBoxes_);
    applySVM(segments);
    //ocrRead(boundingBoxes_);
    //ocrRead(boxesBothSides_);
    //showBoundingBoxes(boxesBothSides_);
    //showBoundingBoxes(boundingBoxes_);
    //overlayText(boxesBothSides_, wordsBothSides_);

    textDisplayOffset_ = 1;
}*/

void DetectText::read(const char* path){
    vector<Mat> segments = segment(boundingBoxes_);
    applySVM(segments, path);
}

vector<Rect>&
DetectText::getBoundingBoxes(Mat& image) {
    
    filename_ = string("streaming.jpg");
    originalImage_ = image;
    mode_ = STREAM;
    
    Mat imGray(originalImage_.size(), CV_8UC1, Scalar(0));
    cvtColor(originalImage_, imGray, CV_RGB2GRAY);
    
    boundingBoxes_.clear();
    boxesBothSides_.clear();
    wordsBothSides_.clear();
    boxesScores_.clear();
    
    preprocess(imGray);
    firstPass_ = true;
    pipeline(1);
    firstPass_ = false;
    pipeline(-1);
    
    overlapBoundingBoxes(boundingBoxes_);
    
    return boundingBoxes_;
    
}

/**
 * @image imagen en blanco y negro
 * Se establecen los parámetros límite, como máxima anchura, máxima altura de letra
 * y mínima altura de letra
 **/
void DetectText::preprocess(Mat& image) {
    //cout << "preprocessing: " << filename_ << endl;
    
    if (mode_ == IMAGE) {
        int slashIndex = -1;
        int dotIndex = -1;
        for (size_t i = filename_.length() - 1; i != 0; i--) {
            if (dotIndex == -1 && filename_[i] == '.')
                dotIndex = i;
            if (slashIndex == -1 && filename_[i] == '/')
                slashIndex = i;
        }
        outputPrefix_ = filename_.substr(slashIndex + 1, dotIndex - slashIndex - 1);
        cout << "outputPrefix: " << outputPrefix_ << endl;
    }
    
    image_ = image;
    //	bilateralFilter(image, image_, 7, 20, 50); // prosilica sensor noise
    
    maxStrokeWidth_ = round(20 * (float) (max(image.cols, image.rows)) / 1000);
    initialStrokeWidth_ = maxStrokeWidth_ * 2;
    maxLetterHeight_ = 600;
    minLetterHeight_ = 10;
}

void DetectText::showImage(string name, Mat img){
    imshow(name, img);
    waitKey(0);
}

void DetectText::pipeline(int blackWhite) {
    if (blackWhite == 1) {
        fontColor_ = BRIGHT;
    } else if (blackWhite == -1) {
        fontColor_ = DARK;
    } else {
        cout << "blackwhite should only be +/-1" << endl;
        assert(false);
    }
    // initialize swtmap with large values
    double start_time;
    double time_in_seconds;
    
    start_time = clock();
    Mat swtmap(image_.size(), CV_32FC1, Scalar(initialStrokeWidth_));
    strokeWidthTransform(image_, swtmap, blackWhite);
    
    start_time = clock();
    Mat ccmap(image_.size(), CV_32FC1, Scalar(-1));
    componentsRoi_.clear();
    nComponent_ = connectComponentAnalysis(swtmap, ccmap);
    
    start_time = clock();
    identifyLetters(swtmap, ccmap);
    
    start_time = clock();
    groupLetters(swtmap, ccmap);
    
    start_time = clock();
    chainPairs(ccmap);

    //showEdgeMap();
    //showSwtmap(swtmap);
    //showCcmap(ccmap);
    //showLetterGroup();
    
    disposal();
}

void DetectText::strokeWidthTransform(const Mat& image, Mat& swtmap,
                                      int searchDirection) {
    
    if (firstPass_) {
        // compute edge map
        Canny(image_, edgemap_, 50, 120);
        
        //compute gradient direction
        Mat dx, dy;
        Sobel(image_, dx, CV_32FC1, 1, 0, 3);
        Sobel(image_, dy, CV_32FC1, 0, 1, 3);
        
        theta_ = Mat(image_.size(), CV_32FC1);
        
        if (edgepoints_.size()) {
            edgepoints_.clear();
        }
        
        for (int y = 0; y < edgemap_.rows; y++) {
            for (int x = 0; x < edgemap_.cols; x++) {
                if (edgemap_.at<unsigned char>(y, x) == 255) {
                    theta_.at<float>(y, x) = atan2(dy.at<float>(y, x),
                                                   dx.at<float>(y, x));
                    edgepoints_.push_back(Point(x, y));
                }
            }
        }
    }
    
    vector < Point > strokePoints;
    updateStrokeWidth(swtmap, edgepoints_, strokePoints, searchDirection,
                      UPDATE);
    
    updateStrokeWidth(swtmap, strokePoints, strokePoints, searchDirection,
                      REFINE);
    
}

void DetectText::updateStrokeWidth(Mat& swtmap, vector<Point>& startPoints,
                                   vector<Point>& strokePoints, int searchDirection, Purpose purpose) {
    //loop through all edgepoints, compute stroke width
    vector<Point>::iterator itr = startPoints.begin();
    vector < Point > pointStack;
    vector<float> SwtValues;
    for (; itr != startPoints.end(); ++itr) {
        pointStack.clear();
        SwtValues.clear();
        float step = 1;
        float iy = (*itr).y;
        float ix = (*itr).x;
        float currY = iy;
        float currX = ix;
        bool isStroke = false;
        float iTheta = theta_.at<float>(*itr);
        pointStack.push_back(Point(currX, currY));
        SwtValues.push_back(swtmap.at<float>(currY, currX));
        while (step < maxStrokeWidth_) {
            float nextY = round(iy + sin(iTheta) * searchDirection * step);
            float nextX = round(ix + cos(iTheta) * searchDirection * step);
            
            if (nextY < 0 || nextX < 0 || nextY >= edgemap_.rows
                || nextX >= edgemap_.cols)
                break;
            
            step = step + 1;
            if (currY == nextY && currX == nextX)
                continue;
            
            currY = nextY;
            currX = nextX;
            
            pointStack.push_back(Point(currX, currY));
            SwtValues.push_back(swtmap.at<float>(currY, currX));
            
            if (edgemap_.at<unsigned char>(currY, currX) == 255) {
                float jTheta = theta_.at<float>(currY, currX);
                if (abs(abs(iTheta - jTheta) - 3.14) < 3.14 / 2) {
                    isStroke = true;
                    if (purpose == UPDATE) {
                        strokePoints.push_back(Point(ix, iy));
                    }
                }
                break;
            }
        }
        
        if (isStroke) {
            float newSwtVal;
            if (purpose == UPDATE) // update swt based on dist between edges
            {
                newSwtVal = sqrt(
                                 (currY - iy) * (currY - iy)
                                 + (currX - ix) * (currX - ix));
            } else if (purpose == REFINE) // refine swt based on median
            {
                nth_element(SwtValues.begin(),
                            SwtValues.begin() + SwtValues.size() / 2,
                            SwtValues.end());
                newSwtVal = SwtValues[SwtValues.size() / 2];
            }
            
            for (size_t i = 0; i < pointStack.size(); i++) {
                swtmap.at<float>(pointStack[i]) = min(
                                                      swtmap.at<float>(pointStack[i]), newSwtVal);
            }
        }
        
    } // end loop through edge points
    
    // set initial upchanged value back to 0
    
    for (int y = 0; y < swtmap.rows; y++) {
        for (int x = 0; x < swtmap.cols; x++) {
            if (swtmap.at<float>(y, x) == initialStrokeWidth_) {
                swtmap.at<float>(y, x) = 0;
            }
        }
    }
    
}

int DetectText::connectComponentAnalysis(const Mat& swtmap, Mat& ccmap) {
    int ccmapInitialVal = ccmap.at<float>(0, 0);
    int offsetY[] = { -1, -1, -1, 0, 0, 1, 1, 1 };
    int offsetX[] = { -1, 0, 1, -1, 1, -1, 0, 1 };
    int nNeighbors = 8;
    int label = 0;
    
    int vectorSize = ccmap.rows * ccmap.cols;
    
    int *pStack = new int[vectorSize * 2];
    int stackPointer;
    
    int *pVector = new int[vectorSize * 2];
    int vectorPointer;
    
    int currentPointX;
    int currentPointY;
    
    for (int y = 0; y < ccmap.rows; y++) {
        for (int x = 0; x < ccmap.cols; x++) {
            bool connected = false;
            if (ccmap.at<float>(y, x) == ccmapInitialVal) {
                vectorPointer = 0;
                stackPointer = 0;
                pStack[stackPointer] = x;
                pStack[stackPointer + 1] = y;
                
                while (stackPointer >= 0) {
                    currentPointX = pStack[stackPointer];
                    currentPointY = pStack[stackPointer + 1];
                    stackPointer -= 2;
                    
                    pVector[vectorPointer] = currentPointX;
                    pVector[vectorPointer + 1] = currentPointY;
                    vectorPointer += 2;
                    
                    for (int i = 0; i < nNeighbors; i++) {
                        
                        int ny = currentPointY + offsetY[i];
                        int nx = currentPointX + offsetX[i];
                        if (ny < 0 || nx < 0 || ny >= ccmap.rows
                            || nx >= ccmap.cols)
                            continue;
                        
                        if (swtmap.at<float>(ny, nx) == 0) {
                            ccmap.at<float>(ny, nx) = -2;
                            continue;
                        }
                        
                        if (ccmap.at<float>(ny, nx) == ccmapInitialVal) {
                            float sw1 = swtmap.at<float>(ny, nx);
                            float sw2 = swtmap.at<float>(y, x);
                            
                            if (max(sw1, sw2) / min(sw1, sw2) <= 3) {
                                ccmap.at<float>(ny, nx) = label;
                                stackPointer += 2;
                                pStack[stackPointer] = nx;
                                pStack[stackPointer + 1] = ny;
                                connected = true;
                            }
                        }
                    } // loop through neighbors
                    
                }
                
                if (connected) {
                    //	assert(vectorPointer <= vectorSize);
                    //	assert(vectorPointer > 0);
                    
                    int minY = ccmap.rows, minX = ccmap.cols, maxY = 0,
                    maxX = 0;
                    int width, height;
                    for (int i = 0; i < vectorPointer; i += 2) {
                        // ROI for each component
                        minY = min(minY, pVector[i + 1]);
                        minX = min(minX, pVector[i]);
                        maxY = max(maxY, pVector[i + 1]);
                        maxX = max(maxX, pVector[i]);
                    }
                    width = maxX - minX + 1;
                    height = maxY - minY + 1;
                    Rect letterRoi(minX, minY, width, height);
                    componentsRoi_.push_back(letterRoi);
                    //assert(label == componentsRoi_.size()-1);
                    
                    label++;
                    
                } else {
                    ccmap.at<float>(y, x) = -2;
                }
            }
        } // loop through ccmap
    }
    
    delete[] pStack;
    delete[] pVector;
    
    return label;
}

void DetectText::identifyLetters(const Mat& swtmap, const Mat& ccmap) {
    assert(static_cast<size_t>(nComponent_) == componentsRoi_.size());
    isLetterComponects_ = new bool[nComponent_];
    vector<float> iComponentStrokeWidth;
    //cout << nComponent_ << "componets" << endl;
    bool *innerComponents = new bool[nComponent_];
    for (size_t i = 0; i < nComponent_; i++) {
        float maxStrokeWidth = 0;
        float sumStrokeWidth = 0;
        float currentStrokeWidth;
        bool isLetter = true;
        
        Rect *itr = &componentsRoi_[i];
        if (itr->height > maxLetterHeight_ || itr->height < minLetterHeight_) {
            isLetterComponects_[i] = false;
            continue;
        }
        float maxY = itr->y + itr->height;
        float minY = itr->y;
        float maxX = itr->x + itr->width;
        float minX = itr->x;
        float increment = abs(itr->width - itr->height) / 2;
        
        // reset the inner components
        memset(innerComponents, 0, nComponent_ * sizeof(bool));
        
        if (itr->width > itr->height) // increase box height
        {
            maxY = min(maxY + increment, static_cast<float>(ccmap.rows));
            minY = max(minY - increment, static_cast<float>(0.0));
        } else // increase box width
        {
            maxX = min(maxX + increment, static_cast<float>(ccmap.cols));
            minX = max(minX - increment, static_cast<float>(0.0));
        }
        
        for (int y = minY; y < maxY; y++)
            for (int x = minX; x < maxX; x++) {
                int component = static_cast<int>(ccmap.at<float>(y, x));
                if (component == static_cast<int>(i)) {
                    currentStrokeWidth = swtmap.at<float>(y, x);
                    iComponentStrokeWidth.push_back(currentStrokeWidth);
                    maxStrokeWidth = max(maxStrokeWidth, currentStrokeWidth);
                    sumStrokeWidth += currentStrokeWidth;
                } else {
                    if (component >= 0) {
                        innerComponents[component] = true;
                    }
                }
            }
        
        float pixelCount = static_cast<float>(iComponentStrokeWidth.size());
        float mean = sumStrokeWidth / pixelCount;
        float variance = 0;
        for (size_t ii = 0; ii < pixelCount; ii++) {
            variance += pow(iComponentStrokeWidth[ii] - mean, 2);
        }
        variance = variance / pixelCount;
        
        // rules & parameters goes here:
        
        isLetter = isLetter && (variance / mean < 1.5);
        
        isLetter = isLetter
        && (sqrt(
                 (pow((double) itr->width, 2)
                  + pow((double) itr->height, 2)))
            / maxStrokeWidth < 10);
        
        // additional rules:
        isLetter = isLetter && (pixelCount / maxStrokeWidth > 5);
        
        isLetter = isLetter && (itr->width < 2.5 * itr->height);
        
        if (countInnerLetterCandidates(innerComponents) - 1 > 5) {
            isLetter = false;
        }
        
        isLetterComponects_[i] = isLetter;
        
        iComponentStrokeWidth.clear();
    }
    delete[] innerComponents;
}

void DetectText::groupLetters(const Mat& swtmap, const Mat& ccmap) {
    componentsMeanIntensity_ = new float[nComponent_];
    componentsMedianStrokeWidth_ = new float[nComponent_];
    isGrouped_ = new bool[nComponent_];
    memset(componentsMeanIntensity_, 0, nComponent_ * sizeof(float));
    memset(componentsMedianStrokeWidth_, 0, nComponent_ * sizeof(float));
    memset(isGrouped_, false, nComponent_ * sizeof(bool));
    
    Mat debug = originalImage_.clone();
    
    for (size_t i = 0; i < nComponent_; i++) {
        if (!isLetterComponects_[i])
            continue;
        
        Rect iRect = componentsRoi_[i];
        
        float iMeanIntensity = getMeanIntensity(ccmap, iRect,
                                                static_cast<int>(i));
        float iMedianStrokeWidth = getMedianStrokeWidth(ccmap, swtmap, iRect,
                                                        static_cast<int>(i));
        
        for (size_t j = i + 1; j < nComponent_; j++) {
            if (!isLetterComponects_[j])
                continue;
            
            Rect jRect = componentsRoi_[j];
            
            // check if horizontal
            bool horizontal = !(iRect.y > jRect.y + jRect.height
                                || jRect.y > iRect.y + iRect.height);
            
            // check if vertical
            bool vertical = !(iRect.x > jRect.x + jRect.width
                              || jRect.x > iRect.x + iRect.width);
            
            if ((!horizontal) && (!vertical))
                continue;
            
            // if there is a tie between horizontal/vertical
            if (horizontal && vertical) {
                if (abs(
                        (iRect.x + iRect.width / 2)
                        - (jRect.x + jRect.width / 2))
                    >= abs(
                           (iRect.y + iRect.height / 2)
                           - (jRect.y + jRect.height / 2))) {
                        horizontal = true;
                        vertical = false;
                    } else {
                        horizontal = false;
                        vertical = true;
                    }
                
            }
            
            // rule 3: distance between characters
            float distance = sqrt(
                                  pow(
                                      (double) (iRect.x + iRect.width / 2 - jRect.x
                                                - jRect.width / 2), 2)
                                  + pow(
                                        (double) (iRect.y + iRect.height / 2
                                                  - jRect.y - jRect.height / 2), 2));
            int distanceRatio = 4;
            if (horizontal) {
                if (distance / max(iRect.width, jRect.width) > distanceRatio)
                    continue;
            } else {
                if (distance / max(iRect.height, jRect.height) > distanceRatio)
                    continue;
            }
            
            float jMeanIntensity = getMeanIntensity(ccmap, jRect,
                                                    static_cast<int>(j));
            float jMedianStrokeWidth = getMedianStrokeWidth(ccmap, swtmap,
                                                            jRect, static_cast<int>(j));
            
            bool isGroup = true;
            
            // rule 1: median of stroke width ratio
            isGroup = isGroup
            && (max(iMedianStrokeWidth, jMedianStrokeWidth)
                / min(iMedianStrokeWidth, jMedianStrokeWidth)) < 2;
            
            // rule 2: height ratio
            isGroup = isGroup
            && (max(iRect.height, jRect.height)
                / min(iRect.height, jRect.height)) < 2;
            
            // rule 4: average color of letters
            isGroup = isGroup && abs(iMeanIntensity - jMeanIntensity) < 10;
            
            if (isGroup) {
                isGrouped_[i] = true;
                isGrouped_[j] = true;
                
                if (horizontal) {
                    horizontalLetterGroups_.push_back(Pair(i, j));
                }
                
                if (vertical) {
                    verticalLetterGroups_.push_back(Pair(i, j));
                }
            }
        } // end for loop j
    } // end for loop i
}

void DetectText::chainPairs(Mat& ccmap) {
    mergePairs(horizontalLetterGroups_, horizontalChains_);
    
    // horizontalChains
    vector < Rect > initialHorizontalBoxes;
    chainToBox(horizontalChains_, initialHorizontalBoxes);
    
    filterBoundingBoxes(initialHorizontalBoxes, ccmap, 4);
    
    boundingBoxes_.insert(boundingBoxes_.end(), initialHorizontalBoxes.begin(),
                          initialHorizontalBoxes.end());
}

void DetectText::chainToBox(vector<vector<int> >& chain,
                            vector<Rect>& boundingBox) {
    for (size_t i = 0; i < chain.size(); i++) {
        if (chain[i].size() < 3)
            continue;
        int minX = image_.cols, minY = image_.rows, maxX = 0, maxY = 0;
        int letterAreaSum = 0;
        int padding = 5;
        
        for (size_t j = 0; j < chain[i].size(); j++) {
            Rect *itr = &componentsRoi_[chain[i][j]];
            letterAreaSum += itr->width * itr->height;
            minX = min(minX, itr->x);
            minY = min(minY, itr->y);
            maxX = max(maxX, itr->x + itr->width);
            maxY = max(maxY, itr->y + itr->height);
        }
        
        // add padding around each box
        minX = max(0, minX - padding);
        minY = max(0, minY - padding);
        maxX = min(image_.cols, maxX + padding);
        maxY = min(image_.rows, maxY + padding);
        
        boundingBox.push_back(Rect(minX, minY, maxX - minX, maxY - minY));
    }
    
}

bool DetectText::spaticalOrder(Rect a, Rect b) {
    return a.y < b.y;
}

void DetectText::filterBoundingBoxes(vector<Rect>& boundingBoxes, Mat& ccmap,
                                     int rejectRatio) {
    vector < Rect > qualifiedBoxes;
    vector<int> components;
    
    for (size_t i = 0; i < boundingBoxes.size(); i++) {
        int isLetterCount = 0;
        int letterArea = 0;
        int nonLetterArea = 0;
        Rect *rect = &boundingBoxes[i];
        
        float width = static_cast<float>(rect->width);
        float height = static_cast<float>(rect->height);
        if (width < 20)
            continue;
        if (max(width, height) / min(width, height) > 20)
            continue;
        
        for (int y = rect->y; y < rect->y + rect->height; y++)
            for (int x = rect->x; x < rect->x + rect->width; x++) {
                int componetIndex = static_cast<int>(ccmap.at<float>(y, x));
                
                if (componetIndex < 0)
                    continue;
                
                if (isLetterComponects_[componetIndex])
                    letterArea++;
                else
                    nonLetterArea++;
                
                if (find(components.begin(), components.end(), componetIndex)
                    == components.end()) {
                    components.push_back(componetIndex);
                    if (isLetterComponects_[componetIndex])
                        isLetterCount++;
                }
            }
        
        // accept patch with few noise inside
        if (letterArea > 3 * nonLetterArea
            || static_cast<int>(components.size())
            < rejectRatio * isLetterCount) {
            qualifiedBoxes.push_back(*rect);
        }
        components.clear();
    }
    boundingBoxes = qualifiedBoxes;
}

void DetectText::overlapBoundingBoxes(vector<Rect>& boundingBoxes) {
    vector < Rect > bigBoxes;
    
    Mat tempMap(image_.size(), CV_32FC1, Scalar(0));
    for (size_t i = 0; i < boundingBoxes.size(); i++) {
        Rect *rect = &boundingBoxes[i];
        for (int y = rect->y; y < rect->y + rect->height; y++)
            for (int x = rect->x; x < rect->x + rect->width; x++) {
                tempMap.at<float>(y, x) = 50;
            }
    }
    
    for (size_t i = 0; i < boundingBoxes.size(); i++) {
        if (tempMap.at<float>(boundingBoxes[i].y + 1, boundingBoxes[i].x + 1)
            != 50)
            continue;
        
        Rect rect;
        floodFill(tempMap, Point(boundingBoxes[i].x, boundingBoxes[i].y),
                  i + 100, &rect);
        
        int padding = 5;
        
        // add padding around each box
        int minX = max(0, rect.x - padding);
        int minY = max(0, rect.y - padding);
        int maxX = min(image_.cols, rect.x + rect.width + padding);
        int maxY = min(image_.rows, rect.y + rect.height + padding);
        
        bigBoxes.push_back(Rect(minX, minY, maxX - minX, maxY - minY));
    }
    
    boundingBoxes = bigBoxes;
}

Mat resizeImage(const Mat& patch, int height, int width, double scale){
    Mat resultResized;
    if (scale == 0) {
        resultResized.create(height,width, CV_8UC3);
        resize(patch, resultResized, resultResized.size(), 0, 0, INTER_CUBIC);
    } else {
        height = patch.rows*scale;
        width = patch.cols*scale;
        resultResized.create(height,width, CV_8UC3);
        resize(patch, resultResized, resultResized.size(), 0, 0, INTER_CUBIC);
    }
    return resultResized;
}

Mat DetectText::equalize(const Mat& patch){
    //Equalize croped image
    Mat grayResult;
    cvtColor(patch, grayResult, CV_BGR2GRAY);
    blur(grayResult, grayResult, Size(3,3));
    //grayResult=histeq(grayResult);
    equalizeHist(grayResult, grayResult);
    return grayResult;
}

vector<Mat> DetectText::segment(vector<Rect>& boundingBoxes){
    sort(boundingBoxes.begin(), boundingBoxes.end(), DetectText::spaticalOrder);
    vector<Mat> segments;
    for (size_t i = 0; i < boundingBoxes.size(); i++) {
        Mat result = equalize(originalImage_(boundingBoxes[i]));
        Mat scaled = resizeImage(result, 60, 320, 0);
        segments.push_back(scaled);
        
        if (mode_ == IMAGE) {
            string result;
            stringstream ss;
            string s;
            ss << "patches/" << i;
            s = ss.str() + ".tiff";
            imwrite(s, scaled);
        }
    }
    
    return segments;
}

void DetectText::applySVM(vector<Mat>& segments, String path) {
    //SVM for each plate region to get valid car plates
    //Read file storage.
    FileStorage fs;
    //fs.open("SVM.xml", FileStorage::READ);
    fs.open(path, FileStorage::READ);
    Mat SVM_TrainingData;
    Mat SVM_Classes;
    fs["TrainingData"] >> SVM_TrainingData;
    fs["classes"] >> SVM_Classes;
    //Set SVM params
    CvSVMParams SVM_params;
    SVM_params.svm_type = CvSVM::C_SVC;
    SVM_params.kernel_type = CvSVM::LINEAR; //CvSVM::LINEAR;
    SVM_params.degree = 0;
    SVM_params.gamma = 1;
    SVM_params.coef0 = 0;
    SVM_params.C = 1;
    SVM_params.nu = 0;
    SVM_params.p = 0;
    SVM_params.term_crit = cvTermCriteria(CV_TERMCRIT_ITER, 1000, 0.01);
    //Train SVM
    CvSVM svmClassifier(SVM_TrainingData, SVM_Classes, Mat(), Mat(), SVM_params);
    
    //Classify words or no words
    vector<Rect> posible_regions = boundingBoxes_;
    sort(posible_regions.begin(), posible_regions.end(), DetectText::spaticalOrder);
    for(int i=0; i< segments.size(); i++) {
        Mat img=segments[i];
        Mat p= img.reshape(1, 1);
        p.convertTo(p, CV_32FC1);
        
        int response = (int)svmClassifier.predict( p );

        if(response==1)
            boxesBothSides_.push_back(posible_regions[i]);
    }
}

Mat DetectText::filterPatch(const Mat& patch) {
    Mat result;

    //Binarisation
    threshold(patch, patch, 0, 255, THRESH_BINARY | CV_THRESH_OTSU);
    
    //Morphology transformation
    //MORPH_RECT
    int morph_elem = 0;
    int morph_size = 63;
    //BLACK_HAT
    int morph_operator = 4;
    
    Mat element = getStructuringElement( morph_elem, Size( 2*morph_size + 1, 2*morph_size+1 ), Point( morph_size, morph_size ) );
    
    // Since MORPH_X : 2,3,4,5 and 6
    int operation = morph_operator + 2;
    /// Apply the specified morphology operation
    morphologyEx( patch, patch, operation, element );

    result = patch;
    return result;
}

void DetectText::disposal() {
    delete[] isLetterComponects_;
    delete[] isGrouped_;
    delete[] componentsMeanIntensity_;
    delete[] componentsMedianStrokeWidth_;
    
    componentsRoi_.clear();
    innerComponents_.clear();
    horizontalLetterGroups_.clear();
    verticalLetterGroups_.clear();
    horizontalChains_.clear();
    verticalChains_.clear();
}

/********************* helper functions ***************************/

string& DetectText::trim(string& str) {
    // Trim Both leading and trailing spaces
    
    // Find the first character position after
    // excluding leading blank spaces
    size_t startpos = str.find_first_not_of(" \t");
    // Find the first character position from reverse af
    size_t endpos = str.find_last_not_of(" \t");
    // if all spaces or empty return an empty string
    if ((string::npos == startpos) || (string::npos == endpos))
        str = "";
    else
        str = str.substr(startpos, endpos - startpos + 1);
    return str;
}

/*--------------------------------------------------------*\
 *	display functions
 \*--------------------------------------------------------*/

void DetectText::showEdgeMap() {
    if (mode_ == IMAGE) {
        if (firstPass_)
            imwrite("1."+outputPrefix_+"_outedgemap.png", edgemap_);
    }
}

void DetectText::showSwtmap(Mat& swtmap) {
    if (mode_ == IMAGE) {
        if (firstPass_)
            imwrite("2."+outputPrefix_+"swtmap1.png", swtmap * 10);
        else
            imwrite("5."+outputPrefix_+"swtmap2.png", swtmap * 10);
    }
}

void DetectText::showCcmap(Mat& ccmap) {
    Mat ccmapLetters = ccmap;
    /*Mat ccmapLetters = ccmap * (1.0 / static_cast<float>(nComponent_));
    for (size_t i = 0; i < nComponent_; ++i) {
        Rect *itr = &componentsRoi_[i];
        rectangle(ccmapLetters, Point(itr->x, itr->y),
                  Point(itr->x + itr->width, itr->y + itr->height), Scalar(0.5));
    }*/
    if (mode_ == IMAGE) {
        if (firstPass_)
            imwrite("3."+outputPrefix_+"ccmap1.png", ccmapLetters * nComponent_);
        else
            imwrite("6."+outputPrefix_+"ccmap2.png", ccmapLetters * nComponent_);
    }
}

void DetectText::showLetterGroup() {
    Mat output = originalImage_.clone();
    Scalar scalar;
    if (firstPass_)
        scalar = Scalar(0, 255, 0);
    else
        scalar = Scalar(0, 0, 255);
    
    for (size_t i = 0; i < nComponent_; ++i) {
        if (isGrouped_[i]) {
            Rect *itr = &componentsRoi_[i];
            rectangle(output, Point(itr->x, itr->y),
                      Point(itr->x + itr->width, itr->y + itr->height), scalar,
                      2);
        }
    }
    
    if (mode_ == IMAGE) {
        if (firstPass_)
            imwrite("4."+outputPrefix_ + "_group1.png", output);
        else
            imwrite("7."+outputPrefix_ + "_group2.png", output);
    }
}

void DetectText::showBoundingBoxes(vector<Rect>& boundingBoxes) {
    Scalar scalar(0, 0, 255);
    detection_ = originalImage_.clone();
    
    for (size_t i = 0; i < boundingBoxes.size(); i++) {
        Rect *rect = &boundingBoxes[i];
        rectangle(detection_, Point(rect->x, rect->y),
                  Point(rect->x + rect->width, rect->y + rect->height), scalar,
                  3);
    }
    if (mode_ == IMAGE) {
        imwrite("9."+outputPrefix_ + "_showBoxes.png", detection_);
    }
}

void DetectText::showBoundingBoxes(vector<Rect>& boundingBoxes,
                                   vector<bool>& boxInbox) {
    assert(boundingBoxes.size() == boxInbox.size());
    Scalar scalar;
    scalar = Scalar(0, 0, 255);
    
    for (size_t i = 0; i < boundingBoxes.size(); i++) {
        if (boxInbox[i] == true)
            continue;
        Rect *rect = &boundingBoxes[i];
        rectangle(detection_, Point(rect->x, rect->y),
                  Point(rect->x + rect->width, rect->y + rect->height), scalar,
                  3);
    }
}

inline int DetectText::countInnerLetterCandidates(bool* array) {
    int count = 0;
    for (size_t i = 0; i < nComponent_; i++) {
        if (array[i] && isLetterComponects_[i]) {
            count++;
        }
    }
    return count;
}

float DetectText::getMeanIntensity(const Mat& ccmap, const Rect& rect,
                                   int element) {
    assert(element >= 0);
    if (componentsMeanIntensity_[element] == 0) {
        float sum = 0;
        float count = 0;
        float felement = static_cast<float>(element);
        for (int y = rect.y; y < rect.y + rect.height; y++)
            for (int x = rect.x; x < rect.x + rect.width; x++) {
                if (ccmap.at<float>(y, x) == felement) {
                    sum += static_cast<float>(image_.at<unsigned char>(y, x));
                    count = count + 1;
                }
            }
        componentsMeanIntensity_[element] = sum / count;
    }
    
    return componentsMeanIntensity_[element];
}

float DetectText::getMedianStrokeWidth(const Mat& ccmap, const Mat& swtmap,
                                       const Rect& rect, int element) {
    
    assert(element >= 0);
    assert(isLetterComponects_[element]);
    if (componentsMedianStrokeWidth_[element] == 0) {
        vector<float> SwtValues;
        
        float felement = static_cast<float>(element);
        for (int y = rect.y; y < rect.y + rect.height; y++)
            for (int x = rect.x; x < rect.x + rect.width; x++) {
                if (ccmap.at<float>(y, x) == felement) {
                    SwtValues.push_back(swtmap.at<float>(y, x));
                }
            }
        
        nth_element(SwtValues.begin(), SwtValues.begin() + SwtValues.size() / 2,
                    SwtValues.end());
        
        componentsMedianStrokeWidth_[element] = SwtValues[SwtValues.size() / 2];
        
    }
    return componentsMedianStrokeWidth_[element];
}

void DetectText::mergePairs(const vector<Pair>& groups,
                            vector<vector<int> >& chains) {
    vector < vector<int> > initialChains;
    initialChains.resize(groups.size());
    for (size_t i = 0; i < groups.size(); i++) {
        vector<int> temp;
        temp.push_back(groups[i].left);
        temp.push_back(groups[i].right);
        initialChains[i] = temp;
    }
    
    while (mergePairs(initialChains, chains)) {
        initialChains = chains;
        chains.clear();
    }
}

bool DetectText::mergePairs(const vector<vector<int> >& initialChains,
                            vector<vector<int> >& chains) {
    if (chains.size())
        chains.clear();
    
    bool merged = false;
    int *mergedToChainBitMap = new int[initialChains.size()];
    memset(mergedToChainBitMap, -1, initialChains.size() * sizeof(int));
    for (size_t i = 0; i < initialChains.size(); i++) // chain i
    {
        if (mergedToChainBitMap[i] != -1)
            continue;
        
        for (size_t j = i + 1; j < initialChains.size(); j++) // chain j
        {
            // match elements in chain i,j
            for (size_t ki = 0; ki < initialChains[i].size(); ki++) {
                for (size_t kj = 0; kj < initialChains[j].size(); kj++) {
                    // found match				
                    if (initialChains[i][ki] == initialChains[j][kj]) {
                        merged = true;
                        // j already merged with others
                        if (mergedToChainBitMap[j] != -1) {
                            merge(initialChains[i],
                                  chains[mergedToChainBitMap[j]]);
                            
                            mergedToChainBitMap[i] = mergedToChainBitMap[j];
                        } else // start a new chain
                        {
                            vector<int> newChain;
                            merge(initialChains[i], newChain);
                            merge(initialChains[j], newChain);
                            chains.push_back(newChain);
                            mergedToChainBitMap[i] = chains.size() - 1;
                            mergedToChainBitMap[j] = chains.size() - 1;
                        }
                        break;
                    }
                }
                if (mergedToChainBitMap[i] != -1
                    && mergedToChainBitMap[j] != -1)
                    break;
            }
        }
        
        // comparing with all other chains, not found a match
        if (mergedToChainBitMap[i] == -1) {
            chains.push_back(initialChains[i]);
            mergedToChainBitMap[i] = chains.size() - 1;
        }
        
    }
    
    if (!merged) {
        chains = initialChains;
    }
    
    // dispose resourse
    delete[] mergedToChainBitMap;
    
    return merged;
}

void DetectText::merge(const vector<int>& token, vector<int>& chain) {
    vector<int>::iterator it;
    for (size_t i = 0; i < token.size(); i++) {
        it = find(chain.begin(), chain.end(), token[i]);
        if (it == chain.end()) {
            chain.push_back(token[i]);
        }
    }
}
