package com.vishal.learn.gcppubsub;

import com.vishal.learn.gcppubsub.GcpPubsubApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class WebAppController {

 @Autowired
public GcpPubsubApplication.PubsubOutboundGateway messageGateway;

    @PostMapping("/publishMessage")
    public RedirectView publishMessage(@RequestParam("message") String message) {
        messageGateway.sendToPubsub(message);
        return new RedirectView("/");
    }
}
