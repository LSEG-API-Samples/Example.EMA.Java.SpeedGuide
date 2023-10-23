package com.lseg.ema.example.gui;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import com.lseg.ema.example.gui.SpeedGuide.StatusIndicator;
import com.lseg.ema.example.gui.view.SpeedGuideViewController;

public class StatusLogHandler extends StreamHandler {
    private SpeedGuideViewController m_viewController;

	public void setViewController(SpeedGuideViewController viewController) {
		m_viewController = viewController;
	}

    @Override
    public void publish(LogRecord record)
    {
    	if ( m_viewController != null && (record.getLevel() == Level.WARNING || record.getLevel() == Level.SEVERE) )
    	{
    		final String TOKEN_TEXT = "Text:";
    		final String TOKEN_DETAILS = "Error text";
    		final String TOKEN_STATE = "State:";

    		// Ignore the error reporting no EmaConfig.xml found
    		if ( !record.getMessage().contains("EmaConfig.xml"))
    		{
        		String[] lines = record.getMessage().split("\n");

        		String text = "";
        		for (String line : lines)
        		{
        			if (line.contains(TOKEN_TEXT))
        				text += line.substring(line.indexOf(TOKEN_TEXT)+TOKEN_TEXT.length()).trim() + SpeedGuide.NEWLINE;
        			else if (line.contains(TOKEN_DETAILS))
        				text += line.substring(line.indexOf(TOKEN_DETAILS)+TOKEN_DETAILS.length()).trim() + SpeedGuide.NEWLINE;
        			else if (line.contains(TOKEN_STATE))
        				text += line.substring(line.indexOf(TOKEN_STATE)+TOKEN_STATE.length()).trim() + SpeedGuide.NEWLINE;
        		}

    	        // Send errors to GUI status
        		if (!text.isEmpty() ) {
        			int pos = text.lastIndexOf(SpeedGuide.NEWLINE);
        			if ( pos >= 0)
        				text = text.substring(0, pos);

        			m_viewController.updateStatus(text, StatusIndicator.RESPONSE_ERROR);
        		}
        		else
        			m_viewController.updateStatus(record.getMessage(), StatusIndicator.RESPONSE_ERROR);
    		}
    	}
        super.publish(record);
    }


    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public void close() throws SecurityException {
        super.close();
    }
}
