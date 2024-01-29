package com.cloudpos.convertdifffinger.mgr;
import com.upek.android.ptapi.PtConstants;

public class PtHelper
{
	/** 
     * Create message for given GUI callback message
     */
	public static final String GetGuiStateCallbackMessage(int guiState, int message, byte progress)
	{
		String s = null;
		
		if((guiState & PtConstants.PT_MESSAGE_PROVIDED) != PtConstants.PT_MESSAGE_PROVIDED)
        {
			return s;
        }
        switch(message)
        {
        // Generic GUI messages:            
        case PtConstants.PT_GUIMSG_PUT_FINGER:
        case PtConstants.PT_GUIMSG_PUT_FINGER2:
        case PtConstants.PT_GUIMSG_PUT_FINGER3:
        case PtConstants.PT_GUIMSG_PUT_FINGER4:
        case PtConstants.PT_GUIMSG_PUT_FINGER5:
            s = "Put Finger";
            break;
        case PtConstants.PT_GUIMSG_KEEP_FINGER:
            s = "Keep Finger";
            break;
        case PtConstants.PT_GUIMSG_REMOVE_FINGER:
            s = "Lift Finger";
            break;
        case PtConstants.PT_GUIMSG_BAD_QUALITY:
        case PtConstants.PT_GUIMSG_TOO_STRANGE:
            s = "Bad Quality";
            break;
        case PtConstants.PT_GUIMSG_TOO_LEFT:
            s = "Too Left";
            break;
        case PtConstants.PT_GUIMSG_TOO_RIGHT:
            s = "Too Right";
            break;
        case PtConstants.PT_GUIMSG_TOO_LIGHT:
            s = "Too Light";
            break;
        case PtConstants.PT_GUIMSG_TOO_DRY:
            s = "Too Dry";
            break;
        case PtConstants.PT_GUIMSG_TOO_SMALL:
            s = "Too Small";
            break;
        case PtConstants.PT_GUIMSG_TOO_SHORT:
            s = "Too Short";
            break;
        case PtConstants.PT_GUIMSG_TOO_HIGH:
            s = "Too High";
            break;
        case PtConstants.PT_GUIMSG_TOO_LOW:
            s = "Too Low";
            break;
        case PtConstants.PT_GUIMSG_TOO_FAST:
            s = "Too Fast";
            break;
        case PtConstants.PT_GUIMSG_TOO_SKEWED:
            s = "Too Skewed";
            break;
        case PtConstants.PT_GUIMSG_TOO_DARK:
            s = "Too Dark";
            break;
        case PtConstants.PT_GUIMSG_BACKWARD_MOVEMENT:
            s = "Backward movement";
            break;
        case PtConstants.PT_GUIMSG_JOINT_DETECTED:
            s = "Joint Detectected";
            break;
        case PtConstants.PT_GUIMSG_CENTER_AND_PRESS_HARDER:
            s = "Press Center and Harder";
            break;
        case PtConstants.PT_GUIMSG_PROCESSING_IMAGE:
            s = "Processing Image";
            break;
        // Enrollment specific:
        case PtConstants.PT_GUIMSG_NO_MATCH:
            s = "Template extracted from last swipe doesn't match previous one";
            break;
        case PtConstants.PT_GUIMSG_ENROLL_PROGRESS:
            s = "Enrollment progress";
            if((guiState & PtConstants.PT_PROGRESS_PROVIDED) == PtConstants.PT_PROGRESS_PROVIDED)
            {
                s += ": " + progress + '%';
            }

        default:
            break;
        }
        
        return s;
	}

}
