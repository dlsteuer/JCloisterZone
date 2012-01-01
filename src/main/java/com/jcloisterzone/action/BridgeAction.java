package com.jcloisterzone.action;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.rmi.Client2ClientIF;

public class BridgeAction extends FeatureAction {

	@Override
	public void perform(Client2ClientIF server, Position p, Location loc) {
		server.deployBridge(p, loc);
	}

}
