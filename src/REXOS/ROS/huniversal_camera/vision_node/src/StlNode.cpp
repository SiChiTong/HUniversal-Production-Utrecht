#include "vision_node/StlNode.h"

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/objdetect/objdetect.hpp>

#include <iostream>

using namespace opencv;

StlNode::StlNode(){}


vector<vector<Point>> FeatureFactory::findConnectedComponents(const Mat& image){
    vector<vector<Point>> blobs;
    Mat workingImage = ImageFactory::applyOtsuThreshold(image);
    // Fill the label_image with the blobs
    // 0  - background
    // 1  - unlabelled foreground
    // 2+ - labelled foreground

    threshold(workingImage,workingImage,0.0,1.0,THRESH_BINARY);
    workingImage.convertTo(workingImage, CV_32SC1);

    //TODO(Edwin): Do i really need this?

    int labelCount = 2; // starts at 2 because 0,1 are used already

    for(int y=0; y < workingImage.rows; y++) {
        int *row = (int*)workingImage.ptr(y);
        for(int x=0; x < workingImage.cols; x++) {
            if(row[x] != 1) {
                continue;
            }
            cv::Rect rect;
            cv::floodFill(workingImage, cv::Point(x,y), labelCount, &rect, 0, 0, 4);
            std::vector <cv::Point> blob;
            for(int i=rect.y; i < (rect.y+rect.height); i++) {
                int *row2 = (int*)workingImage.ptr(i);
                for(int j=rect.x; j < (rect.x+rect.width); j++) {
                    if(row2[j] != labelCount) {
                        continue;
                    }

                    blob.push_back(Point(j,i));
                }
            }
            if(blob.size() > 200){
                blobs.push_back(blob);
            }
            labelCount++;
        }
    }
    return blobs;
}

vector<vector<Point> > FeatureFactory::getContours(const Mat &image){
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    findContours(image,contours,hierarchy,CV_RETR_CCOMP,CV_CHAIN_APPROX_SIMPLE);
    return contours;
}

pair<vector<vector<Point>>, vector<Vec4i>> FeatureFactory::getContoursHierarchy(const Mat &image){
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    findContours(image,contours,hierarchy,CV_RETR_CCOMP,CV_CHAIN_APPROX_SIMPLE);
    return make_pair(contours,hierarchy);
}

vector<vector<Point>> FeatureFactory::getHoles(const pair<vector<vector<Point> >, vector<Vec4i> >& contours){
    vector<vector<Point>> holes;
    for(int i = 0; i < contours.first.size(); ++i){
        // The size check is to filter out the smallest insignificant holes
        if(contours.second[i][3] != -1 && contours.first[i].size() > 10*M_PI){
            holes.push_back(contours.first[i]);
        }
    }
    for(int i = 0; i < holes.size();++i){
        cout << "Holes check: " << holes[i].size() << endl;
    }
    return holes;
}

vector<vector<Point> > FeatureFactory::getHoles(const Mat &image){
    return getHoles(getContoursHierarchy(image));
}

vector<VisionObject> ImageFactory::filterObjects(vector<vector<Point>>& objects,Mat& image){
    vector<VisionObject> visionObjects;
    int x,y;
    int minx = 1080,maxx = 0;
    int miny = 1920,maxy = 0;
    for(int i = 0; i < objects.size();++i){
        if(objects[i].size() < 100000 && objects[i].size() > 500){
            minx = 1080,maxx = 0;
            miny = 1920,maxy = 0;
            for(int j = 0; j < objects[i].size();j++){
                x = objects[i][j].x;
                y = objects[i][j].y;
                if(x > maxx){
                    maxx = x;
                }
                if(x < minx){
                    minx = x;
                }
                if(y > maxy){
                    maxy = y;
                }
                if( y < miny){
                    miny = y;
                }
            }
            //create a small border for some extra space around the object in the objectimage
            maxx+=10;
            maxy+=10;
            minx-=10;
            miny-=10;
            Mat objectImage = Mat::zeros(maxy - miny,maxx-minx,CV_8U);
            for(int j = 0; j < objects[i].size();++j){
                //TODO(Edwin): zoek uit waarom deze check nodig is...
                if(objects[i][j].x - minx < objectImage.size().width && objects[i][j].y - miny < objectImage.size().height){
                    objectImage.at<uchar>(objects[i][j].y - miny,objects[i][j].x - minx) =
                            image.at<uchar>(objects[i][j].y,objects[i][j].x);
                }
            }
            imshow("testttt",objectImage);

            VisionObject filteredObject;
            filteredObject.data = objects[i];
            filteredObject.objectImage = objectImage;
            visionObjects.push_back(filteredObject);
        }
    }
    return visionObjects;
}


void StlNode::handleFrame(cv::Mat& frame,cv::Mat* testframe){

    if(!frame.empty()){
        cv::namedWindow("Testing clone mat");
        cv::imshow("Testing clone mat",frame);
    }else{
        REXOS_INFO("STL VISION: Given clone frame was empty.");
    }
    if(!&testframe.empty()){
        cv::namedWindow("Testing pointer mat");
        cv::imshow("Testing pointer mat",&testframe);
    }else{
        REXOS_INFO("STL VISION: Given pointer frame was empty");
    }

}