

Description of blocks.json format:
{ // accolades around entire file (as the JSON standard dictates)
  "manufacturer" : "/your company name, may include spaces/",
  "slogan" : "/a one-line description of your company/"
  "blocks" : { // all fields are optional
    "/name of the block, may include spaces/" : {
      "graphic" : "/exact file name, relative to this file. Use .. to access parent map/", // mesh used for drawing. Excludes any kind of studs
      "collision" : "/exact file name, relative to this file. Use .. to access parent map/", // mesh used for collisions, may be the same as for drawing
      "size" : [/integer width x/, /integer width y/, /integer height z/], encompassing hitbox
      // if size is missing, it is calculated from collision. If collision is missing, a hitbox is created based on size. At least one of these is required.
      "mass" : /exact weight/, // in units of a 1x1x1 block
      "topPoints" : [ // coordinates where buds are. Note: this is one higher than where these buds are placed on
          [/integer x/, /integer y/, /integer z/] // multiple can be added, separated with commas
        ],
      "bottomPoints" : [ // coordinates where buds can be attached
          [/integer x/, /integer y/, /integer z/]
        ],
      "wheelConnections" : [{ // optional connection data for wheels
        "size" : /exact radius of the axis/, // used to determine which wheels fit
        "hingeOffset" : [/exact x/, /exact y/, /exact z/] // coordinates of where the wheels are connected
        "axis" : "/x, y or z/" // the axis to rotate the wheel around
      }] // multiple connections can be added, separated with commas
      "hidden" : /true or false/, // optional. When false or missing, this block is shown in the list.
    } // multiple blocks can be added, separated with commas
  }
  "joints" : { // all fields are required
    "/name of the joint, may include spaces/" : {
      "bottom" : "/name of the bottom part as defined in blocks/", // usually, both bottom and top have ("hidden" : true)
      "top" : "/name of the top part as defined in blocks/",
      // for both top and bottom, also [/integer width x/, /integer width y/, /integer height z/] is accepted, where a basic block of the given dimensions is generated.
      "axis" : "/x, y or z/" // the axis to rotate around
      // when both minAngle and maxAngle are left out, the rotation is unlimited
      "minAngle" : /minimum angle/ // in degrees
      "maxAngle" : /maximum angle/ // in degrees
    }
  }
  "wheels" : {
      "/name of the wheel, may include spaces/" : {
        "graphic" : "/exact file name, relative to this file. Use .. to access parent map/", // mesh used for drawing.
        "radius" : /exact radius of this wheel/,
        "connectionSize" : /exact radius of the axis/, // used to determine which connections this fit
        "mass" : /exact weight/
      }
    }
}
End description of blocks.json format
