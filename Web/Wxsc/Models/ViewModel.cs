using System.ComponentModel.DataAnnotations;

namespace Wxsc.Models
{

    public class LoginViewModel
    {
        [Required]
        [Display(Name = "邮箱")]
        public string Email { get; set; }

        [Required]
        [DataType(DataType.Password)]
        [Display(Name = "密码")]
        public string Password { get; set; }

        [Display(Name = "记住登录状态")]
        public bool RememberMe { get; set; }
    }

    public class RegisterViewModel
    {
        [Required]
        [Display(Name = "用户名")]
        public string UserName { get; set; }

        [Required]
        [EmailAddress]
        [Display(Name = "Email")]
        public string Email { get; set; }

        [Required]
        [StringLength(16, ErrorMessage = "{0} 必须大于 {2} 位", MinimumLength = 6)]
        [DataType(DataType.Password)]
        [Display(Name = "密码")]
        public string Password { get; set; }

        [DataType(DataType.Password)]
        [Display(Name = "确认密码")]
        [Compare("Password", ErrorMessage = "两次密码不一致")]
        public string ConfirmPassword { get; set; }
    }

    public class UserViewModel
    {
        [Required]
        [Display(Name = "昵称")]
        public string NickName { get; set; }

        [Required]
        [Display(Name = "年龄")]
        public string Age { get; set; }
        [Required]
        [Display(Name = "性别")]
        public string Sex { get; set; }
        [Required]
        [Display(Name = "身高")]
        public string High { get; set; }
        [Required]
        [Display(Name = "体重")]
        public string Weight { get; set; }
        [Required]
        [Display(Name = "是否高血糖")]
        public string IsHighSugar { get; set; }
        [Required]
        [Display(Name = "是否肥胖")]
        public string IsHighFat { get; set; }
    }

}
