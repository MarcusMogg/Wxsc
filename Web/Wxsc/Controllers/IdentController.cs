using System;
using System.IO;
using System.Linq;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using TensorFlow;
using Wxsc.Models;
using Wxsc.TF;

namespace Wxsc.Controllers
{
    [AllowAnonymous]
    [Route("api/[controller]")]
    public class IdentController : Controller
    {
        private readonly MDbContext _context;
        private readonly IHostingEnvironment _env;
        private readonly TfData _tf;
        public IdentController(MDbContext context, IHostingEnvironment env)
        {
            _context = context;
            _env = env;
            _tf = TfData.GeTfData();
        }

        [HttpPost]
        public ActionResult<MenuItem> Post([FromForm]IFormFile file)
        {
            if (file == null)
            {
                return Ok(new
                {
                    Request = new Result(1, "file error")

                });
            }

            var ext = Path.GetExtension(file.FileName);
            var avatarpath = Path.Combine("images", DateTime.Now.DayOfYear + ext);
            var filepath = Path.Combine(_env.WebRootPath, avatarpath);
            using (FileStream fs = new FileStream(filepath, FileMode.Create))
            {
                file.CopyTo(fs);
                fs.Flush();
            }

            var tensor =ImageUtil.CreateTensorFromImageFile(filepath);
            using (var sess = new TFSession(_tf.Graph))
            {
                var runner = sess.GetRunner();
                
                runner.AddInput(_tf.Graph["input"][0], tensor);
                runner.AddInput(_tf.Graph["keep_prob"][0], 1.0f);
                runner.AddInput(_tf.Graph["is_training"][0], false);
                runner.Fetch(_tf.Graph["ArgMax"][0]);
                var r = runner.Run();
                var v = r[0];
                var res = ((long[])v.GetValue())[0];

                var item = from i in _context.MenuItems where i.Name == _tf.Labels[(int)res] select i;
                if (!item.Any())
                {
                    return Ok(new
                    {
                        Request = new Result(1, "Not found")
                    });
                }
                return Ok(new
                {
                    Request = new Result(0, "Success"),
                    id = res,
                    item = item.First()
                });
            }
        }

    }
}