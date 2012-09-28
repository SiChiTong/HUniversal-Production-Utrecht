//******************************************************************************
//
//                 Low Cost Vision
//
//******************************************************************************
// Project:        huniplacer
// File:           steppermotor3.h
// Description:    steppermotor driver
// Author:         Lukas Vermond & Kasper van Nieuwland
// Notes:          -
//
// License:        newBSD
//
// Copyright © 2012, HU University of Applied Sciences Utrecht
// All rights reserved.

// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// * Neither the name of the HU University of Applied Sciences Utrecht nor the
// names of its contributors may be used to endorse or promote products
// derived from this software without specific prior written permission.

// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE HU UNIVERSITY OF APPLIED SCIENCES UTRECHT BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//******************************************************************************


#pragma once

#include <queue>
#include <boost/thread.hpp>

#include <huniplacer/motion.h>
#include <huniplacer/modbus_exception.h>
#include <huniplacer/modbus_ctrl.h>
#include <huniplacer/imotor3.h>

namespace huniplacer
{
    /**
     * Exception handler to which modbus_exception's will be passed that occur in motion_thread
     **/ 
    typedef void (*motion_thread_exception_handler)(std::exception& ex);

    class steppermotor3 : public imotor3
    {
        private:
            std::queue<motionf> motion_queue;
            bool thread_running;
            double current_angles[3];
            double deviation[3];
            
            bool idle;
            boost::mutex idle_mutex;
            boost::condition_variable idle_cond;
            
            boost::mutex queue_mutex;
            boost::mutex modbus_mutex;
            
            double min_angle;
            double max_angle;
            
            modbus_ctrl modbus;
            
            motion_thread_exception_handler exhandler;
            boost::thread* motion_thread;
            
            volatile bool powered_on;

            static void motion_thread_func(steppermotor3* owner);
        
            void wait_till_ready(void);
            void motion_float_to_int(motioni& mi, const motionf& mf);
        
        public:
            steppermotor3(modbus_t* context, double min_angle, double max_angle, motion_thread_exception_handler exhandler, const double* deviation);
            virtual ~steppermotor3(void);
        
            void moveto(const motionf& mf, bool async = true);
            void moveSingleMotor(int motorIndex, double angle);
            void moveto_within(const motionf& mf, double time, bool async);
            void stop(void);
            bool wait_for_idle(long timeout = 0);
            bool is_idle(void);
            
            void power_off(void);

            void power_on(void);

            void override_current_angles(double * angles);

            bool is_powerd_on(void);

            void setMotorLimits(double minAngle, double maxAngle);

            void resetCounter(int motorIndex);

            inline double get_min_angle(void) const { return min_angle; }
            inline double get_max_angle(void) const { return max_angle; }
            void set_min_angle(double min_angle);
            void set_max_angle(double max_angle);

            void disableControllerLimitations();
            double get_deviation(int motorIndex){
                assert(motorIndex >= 0 && motorIndex < 3);
                return deviation[motorIndex];
            }

            void set_deviation(const double* deviation);
    };
}