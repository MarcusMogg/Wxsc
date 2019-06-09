using Microsoft.AspNetCore.Identity;

namespace Wxsc.Models
{

    public class User : IdentityUser
    {
        public string NickName { get; set; }
        public int Age { get; set; }
        public int Sex { get; set; }
        public double High { get; set; }
        public double Weight { get; set; }
        public int IsHighSugar { get; set; }
        public int IsHighFat { get; set; }
    }
}
