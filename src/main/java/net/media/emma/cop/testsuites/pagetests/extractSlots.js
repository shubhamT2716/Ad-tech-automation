var jsonObj = [];

if(googletag.pubads()){
googletag.pubads().getSlots().map(s => {
jsonObj.push(
        new Map([
                ["adunitPath", s.getAdUnitPath()], ["sizes", s.getSizes()], ["targetting", s.getTargetingMap()]
                ])
    )
});
}

class AdUnitsData {
    constructor(path, sizes, targetting) {
        this.path = path;
        this.sizes = sizes;
      this.targetting = targetting;
    }
  }

//get slots data in json string format
function getSlots(){

    slotsData = [];

    if(googletag.pubads()){
        googletag.pubads().getSlots().map(s => {

            // console.log(s.getAdUnitPath(), JSON.stringify(s.getSizes(), null, 4), JSON.stringify(s.getTargetingMap(), null, 4));
            slotsData.push( new AdUnitsData(s.getAdUnitPath(), JSON.stringify(s.getSizes(), null, 4), JSON.stringify(s.getTargetingMap(), null, 4)));
   
        });
     
    }

    console.log(JSON.stringify(slotsData, null, 4));
}

